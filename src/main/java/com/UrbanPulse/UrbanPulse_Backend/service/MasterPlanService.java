package com.UrbanPulse.UrbanPulse_Backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.UrbanPulse.UrbanPulse_Backend.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Core engine for Master Plan Mode.
 *
 * Algorithm:
 *  1. Call Gemini to estimate realistic BASELINE metrics for the given city.
 *  2. For each policy phase (in order):
 *       a. Build a prompt describing current state + policy.
 *       b. Call Gemini → get {after metrics, insight}.
 *       c. Thread "after" metrics forward as the next phase's "before".
 *  3. Call Gemini once more for a compound insight across all phases.
 *  4. Assemble the full MasterPlanResponse.
 */
@Service
public class MasterPlanService {

    // ── deterministic color palette per policy key ────────────────────────────
    private static final Map<String, String> POLICY_COLORS = Map.of(
            "add_metro",      "#6366f1",
            "add_park",       "#10b981",
            "remove_parking", "#f59e0b",
            "subsidize_ev",   "#06b6d4",
            "build_highway",  "#ef4444",
            "increase_tax",   "#8b5cf6",
            "custom",         "#a1a1aa"
    );
    private static final String DEFAULT_COLOR = "#64748b";

    private final GeminiService gemini;
    private final ObjectMapper  mapper = new ObjectMapper();

    @Autowired
    public MasterPlanService(GeminiService gemini) {
        this.gemini = gemini;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PUBLIC ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────

    public MasterPlanResponse simulate(MasterPlanRequest request) {

        List<PhaseInput> inputs = request.getPhases();

        // ── Step 1: Baseline ──────────────────────────────────────────────────
        Metrics baseline = fetchBaseline(request.getCity(), request.getBudget());
        int baselineYear = deriveBaselineYear(inputs);

        // ── Step 2: Sequential phase simulation ───────────────────────────────
        List<PhaseResult> results    = new ArrayList<>();
        List<TrajectoryPoint> traj   = new ArrayList<>();

        // Seed trajectory with baseline
        traj.add(toTrajectoryPoint(String.valueOf(baselineYear), baseline));

        Metrics current = baseline;
        for (int i = 0; i < inputs.size(); i++) {
            PhaseInput input = inputs.get(i);
            int duration = phaseDuration(inputs, i);

            PhaseResult result = simulatePhase(input, current, request.getCity(), duration);
            results.add(result);
            traj.add(toTrajectoryPoint(input.getYear(), result.getAfter()));
            current = result.getAfter();
        }

        // ── Step 3: Compound insight ───────────────────────────────────────────
        String compoundInsight = fetchCompoundInsight(request.getCity(), baseline, current, results);

        // ── Step 4: Assemble response ─────────────────────────────────────────
        MasterPlanResponse response = new MasterPlanResponse();
        response.setPlanId(generatePlanId());
        response.setCity(request.getCity());
        response.setPlanName(request.getPlanName() != null ? request.getPlanName()
                : request.getCity() + " Master Plan");
        response.setPhases(results);
        response.setTrajectory(traj);
        response.setCompoundInsight(compoundInsight);
        response.setCityHealthBefore(baseline.healthScore());
        response.setCityHealthAfter(current.healthScore());

        return response;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  STEP 1 — BASELINE
    // ─────────────────────────────────────────────────────────────────────────

    private Metrics fetchBaseline(String city, String budget) {
        String prompt = "You are an urban data expert.\n\n" +
                "City: " + city + "\n" +
                "Budget context: " + (budget != null ? budget : "Medium") + "\n\n" +
                "Estimate the CURRENT urban health metrics for this city as a realistic baseline " +
                "BEFORE any policy interventions. Use real-world knowledge to produce accurate " +
                "starting numbers (e.g., Mumbai should have low traffic score due to congestion, " +
                "Singapore should have high scores overall).\n\n" +
                "STRICT RULES:\n" +
                "* Return ONLY valid JSON — no markdown, no explanation\n" +
                "* All scores are integers 0-100 where 100 = perfect/ideal\n\n" +
                "Format:\n" +
                "{\n" +
                "  \"traffic\":   <int 0-100>,\n" +
                "  \"economy\":   <int 0-100>,\n" +
                "  \"ecology\":   <int 0-100>,\n" +
                "  \"sentiment\": <int 0-100>\n" +
                "}";

        String json = gemini.call(prompt);
        if (json != null) {
            try {
                JsonNode n = mapper.readTree(json);
                return new Metrics(
                        clamp(n.path("traffic").asInt(40)),
                        clamp(n.path("economy").asInt(45)),
                        clamp(n.path("ecology").asInt(45)),
                        clamp(n.path("sentiment").asInt(50))
                );
            } catch (Exception e) {
                System.err.println("[MasterPlanService] Failed to parse baseline: " + e.getMessage());
            }
        }
        // Conservative fallback
        return new Metrics(38, 44, 48, 52);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  STEP 2 — SINGLE PHASE
    // ─────────────────────────────────────────────────────────────────────────

    private PhaseResult simulatePhase(PhaseInput input, Metrics before, String city, int duration) {

        String prompt = "You are an urban planning simulation engine.\n\n" +
                "City: " + city + "\n" +
                "Policy: " + input.getLabel() + " (" + input.getPolicy() + ")\n" +
                "Implementation period: " + duration + " year(s)\n\n" +
                "Current city state (before this policy):\n" +
                "  traffic:   " + before.getTraffic()   + "/100\n" +
                "  economy:   " + before.getEconomy()   + "/100\n" +
                "  ecology:   " + before.getEcology()   + "/100\n" +
                "  sentiment: " + before.getSentiment() + "/100\n\n" +
                "STRICT RULES:\n" +
                "* Scores must be integers 0-100\n" +
                "* Scores can go up OR down — not every policy improves every metric\n" +
                "* The duration of implementation matters: a 2-year window yields less improvement than a 5-year window\n" +
                "* Changes compound on the given starting state — do not reset to baseline\n" +
                "* Return ONLY valid JSON — no markdown, no explanation\n\n" +
                "Format:\n" +
                "{\n" +
                "  \"after\": {\n" +
                "    \"traffic\":   <int 0-100>,\n" +
                "    \"economy\":   <int 0-100>,\n" +
                "    \"ecology\":   <int 0-100>,\n" +
                "    \"sentiment\": <int 0-100>\n" +
                "  },\n" +
                "  \"insight\": \"<one concise sentence describing the most significant effect of this policy>\"\n" +
                "}";

        String json = gemini.call(prompt);

        PhaseResult result = new PhaseResult();
        result.setYear(input.getYear());
        result.setPolicy(input.getPolicy());
        result.setLabel(input.getLabel());
        result.setIcon(input.getIcon());
        result.setColor(POLICY_COLORS.getOrDefault(input.getPolicy(), DEFAULT_COLOR));
        result.setBefore(before);

        if (json != null) {
            try {
                JsonNode n = mapper.readTree(json);
                JsonNode afterNode = n.path("after");
                Metrics after = new Metrics(
                        clamp(afterNode.path("traffic").asInt(before.getTraffic())),
                        clamp(afterNode.path("economy").asInt(before.getEconomy())),
                        clamp(afterNode.path("ecology").asInt(before.getEcology())),
                        clamp(afterNode.path("sentiment").asInt(before.getSentiment()))
                );
                result.setAfter(after);
                result.setInsight(n.path("insight").asText("Impact analysed."));
                return result;
            } catch (Exception e) {
                System.err.println("[MasterPlanService] Failed to parse phase " + input.getPolicy() + ": " + e.getMessage());
            }
        }

        // Minimal fallback — small positive nudge
        result.setAfter(nudge(before));
        result.setInsight("Phase simulation ran in safe fallback mode.");
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  STEP 3 — COMPOUND INSIGHT
    // ─────────────────────────────────────────────────────────────────────────

    private String fetchCompoundInsight(String city, Metrics baseline, Metrics finalState,
                                        List<PhaseResult> phases) {
        StringBuilder policyList = new StringBuilder();
        for (PhaseResult p : phases) {
            policyList.append("  - ").append(p.getYear()).append(": ")
                      .append(p.getLabel()).append("\n");
        }

        String prompt = "You are an expert urban policy analyst synthesising a 10-year master plan.\n\n" +
                "City: " + city + "\n" +
                "Policies implemented (in order):\n" + policyList +
                "\nBaseline city health: traffic=" + baseline.getTraffic() +
                ", economy=" + baseline.getEconomy() +
                ", ecology=" + baseline.getEcology() +
                ", sentiment=" + baseline.getSentiment() + "\n" +
                "Final city health: traffic=" + finalState.getTraffic() +
                ", economy=" + finalState.getEconomy() +
                ", ecology=" + finalState.getEcology() +
                ", sentiment=" + finalState.getSentiment() + "\n\n" +
                "Write 3-4 sentences explaining the COMPOUND effect — how these policies reinforced " +
                "each other to achieve more than any single policy could have. Reference specific " +
                "causal chains (e.g., 'the metro enabled the parking removal') and cite the net metric shifts.\n\n" +
                "STRICT RULES:\n" +
                "* Return ONLY valid JSON — no markdown\n\n" +
                "Format:\n" +
                "{ \"compoundInsight\": \"<your paragraph here>\" }";

        String json = gemini.call(prompt);
        if (json != null) {
            try {
                return mapper.readTree(json).path("compoundInsight").asText();
            } catch (Exception e) {
                System.err.println("[MasterPlanService] Failed to parse compound insight: " + e.getMessage());
            }
        }
        return "The sequential implementation of these policies created compounding structural " +
               "improvements across all urban metrics, transforming " + city + " over the plan horizon.";
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Year before the first policy — shown as the chart baseline label. */
    private int deriveBaselineYear(List<PhaseInput> phases) {
        if (phases == null || phases.isEmpty()) return 2024;
        try {
            return Integer.parseInt(phases.get(0).getYear()) - 1;
        } catch (NumberFormatException e) {
            return 2024;
        }
    }

    /**
     * How many years does phase[i] run before the next phase starts?
     * Last phase gets 3 years by default.
     */
    private int phaseDuration(List<PhaseInput> phases, int i) {
        if (i >= phases.size() - 1) return 3;
        try {
            int thisYear = Integer.parseInt(phases.get(i).getYear());
            int nextYear = Integer.parseInt(phases.get(i + 1).getYear());
            return Math.max(1, nextYear - thisYear);
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    private TrajectoryPoint toTrajectoryPoint(String year, Metrics m) {
        return new TrajectoryPoint(year, m.getTraffic(), m.getEconomy(), m.getEcology(), m.getSentiment());
    }

    /** Ensures a metric value stays within [0, 100]. */
    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    /** Small deterministic positive nudge used as a fallback when AI fails. */
    private Metrics nudge(Metrics m) {
        return new Metrics(
                clamp(m.getTraffic()   + 5),
                clamp(m.getEconomy()   + 4),
                clamp(m.getEcology()   + 4),
                clamp(m.getSentiment() + 5)
        );
    }

    private String generatePlanId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rng = new Random();
        StringBuilder sb = new StringBuilder("PLAN-");
        for (int i = 0; i < 5; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
        return sb.toString();
    }
}
