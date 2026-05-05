package com.campus;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 详细并发对比压测 —— 模拟一次完整搜索请求的端到端流程。
 * 逐层对比串行 vs 并行每个环节的耗时。
 */
public class ConcurrencyBenchmarkTest {

    // ── 与 AsyncConfig 一致的线程池 ──
    private final ThreadPoolExecutor matchExecutor = new ThreadPoolExecutor(
            8, 16, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(256),
            r -> new Thread(r, "match-score"), new ThreadPoolExecutor.CallerRunsPolicy());

    private final ThreadPoolExecutor cfExecutor = new ThreadPoolExecutor(
            4, 8, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(128),
            r -> new Thread(r, "cf-compute"), new ThreadPoolExecutor.CallerRunsPolicy());

    private final ThreadPoolExecutor behaviorExecutor = new ThreadPoolExecutor(
            2, 4, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(512),
            r -> new Thread(r, "behavior"), new ThreadPoolExecutor.DiscardPolicy());

    // ──────────────────────────────────────────────────────
    // 端到端搜索请求对比（20 候选人 + 50 相似用户 + 5 条行为）
    // ──────────────────────────────────────────────────────
    @Test
    public void fullPipelineComparison() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  端到端搜索请求流水线对比");
        System.out.println("  参数: 20候选人 | 50相似用户 | 并发搜索 5 人 | 行为记录 5 条");
        System.out.println("=".repeat(70) + "\n");

        int candidates = 20, similarUsers = 50, concurrentUsers = 5, behaviorCount = 5;
        int rounds = 15, warmup = 5;

        // ── 预热 ──
        for (int i = 0; i < warmup; i++) {
            runFullPipelineSerial(candidates, similarUsers, behaviorCount);
            runFullPipelineParallel(candidates, similarUsers, behaviorCount);
            runConcurrentSearchSerial(candidates, concurrentUsers);
            runConcurrentSearchParallel(candidates, concurrentUsers);
        }

        // ── 逐层对比 ──

        // Layer 1: Content scoring
        long l1Ser = 0, l1Par = 0;
        List<Candidate> cands = buildCandidates(candidates);
        for (int r = 0; r < rounds; r++) {
            l1Ser += runSerial(cands);
            l1Par += runParallel(cands, matchExecutor);
        }

        // Layer 2: CF similarity
        List<Long> cfIds = range(similarUsers);
        long l2Ser = 0, l2Par = 0;
        for (int r = 0; r < rounds; r++) {
            l2Ser += runCFSerial(cfIds);
            l2Par += runCFParallel(cfIds, cfExecutor);
        }

        // Layer 3: Behavior async
        long l3Sync = 0, l3Async = 0;
        AtomicInteger done = new AtomicInteger(0);
        for (int r = 0; r < rounds; r++) {
            done.set(0);
            long t0 = System.currentTimeMillis();
            for (int i = 0; i < behaviorCount; i++) {
                behaviorWriteSync();
            }
            l3Sync += (System.currentTimeMillis() - t0);

            t0 = System.currentTimeMillis();
            for (int i = 0; i < behaviorCount; i++) {
                behaviorExecutor.execute(() -> { behaviorWriteSync(); done.incrementAndGet(); });
            }
            l3Async += (System.currentTimeMillis() - t0);
        }
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // Layer 4: Concurrent search (simulates multi-user)
        long l4Ser = 0, l4Par = 0;
        ExecutorService tomcat = Executors.newFixedThreadPool(10);
        for (int r = 0; r < rounds; r++) {
            l4Ser += runConcurrentSearchSerial(candidates, concurrentUsers);
            l4Par += runConcurrentSearchParallel(candidates, concurrentUsers);
        }
        tomcat.shutdown();

        // Layer 5: Full pipeline end-to-end
        long fullSer = 0, fullPar = 0;
        for (int r = 0; r < rounds; r++) {
            fullSer += runFullPipelineSerial(candidates, similarUsers, behaviorCount);
            fullPar += runFullPipelineParallel(candidates, similarUsers, behaviorCount);
        }

        // ── 输出报告 ──
        printSeparator("逐层对比 (平均值, " + rounds + " 轮)");

        printRow("Layer", "子系统", "改造前(串行)", "改造后(并行)", "加速比", "延迟↓");
        printDivider();
        printRow("L1", "候选人评分 (20人)",
                fmt(avg(l1Ser, rounds)), fmt(avg(l1Par, rounds)),
                speedup(l1Ser, l1Par), reduction(l1Ser, l1Par));
        printRow("L2", "CF相似度 (50用户)",
                fmt(avg(l2Ser, rounds)), fmt(avg(l2Par, rounds)),
                speedup(l2Ser, l2Par), reduction(l2Ser, l2Par));
        printRow("L3", "行为记录 (5条)",
                fmt(avg(l3Sync, rounds)), fmt(avg(l3Async, rounds)),
                speedup(l3Sync, l3Async), reduction(l3Sync, l3Async));
        printRow("L4", "5用户并发搜索",
                fmt(avg(l4Ser, rounds)), fmt(avg(l4Par, rounds)),
                speedup(l4Ser, l4Par), reduction(l4Ser, l4Par));
        printDivider();
        printRow("ALL", "端到端完整流水线",
                fmt(avg(fullSer, rounds)), fmt(avg(fullPar, rounds)),
                speedup(fullSer, fullPar), reduction(fullSer, fullPar));

        // ── 候选人数梯度对比 ──
        printSeparator("候选人评分梯度对比 (matchScoringExecutor)");
        System.out.printf("%-10s %-14s %-14s %-10s %-10s%n", "候选人数", "改造前", "改造后", "加速比", "延迟↓");
        printGradientDivider();
        for (int n : new int[]{10, 20, 50, 100}) {
            List<Candidate> cs = buildCandidates(n);
            for (int w = 0; w < 3; w++) { runSerial(cs); runParallel(cs, matchExecutor); }
            long sSum = 0, pSum = 0;
            for (int r = 0; r < 10; r++) { sSum += runSerial(cs); pSum += runParallel(cs, matchExecutor); }
            System.out.printf("%-10d %-14s %-14s %-10s %-10s%n",
                    n, fmt(avg(sSum, 10)), fmt(avg(pSum, 10)),
                    speedup(sSum, pSum), reduction(sSum, pSum));
        }

        // ── CF 相似用户梯度对比 ──
        printSeparator("CF相似度梯度对比 (cfComputeExecutor)");
        System.out.printf("%-10s %-14s %-14s %-10s %-10s%n", "相似用户", "改造前", "改造后", "加速比", "延迟↓");
        printGradientDivider();
        for (int n : new int[]{20, 50, 100}) {
            List<Long> ids = range(n);
            for (int w = 0; w < 3; w++) { runCFSerial(ids); runCFParallel(ids, cfExecutor); }
            long sSum = 0, pSum = 0;
            for (int r = 0; r < 10; r++) { sSum += runCFSerial(ids); pSum += runCFParallel(ids, cfExecutor); }
            System.out.printf("%-10d %-14s %-14s %-10s %-10s%n",
                    n, fmt(avg(sSum, 10)), fmt(avg(pSum, 10)),
                    speedup(sSum, pSum), reduction(sSum, pSum));
        }

        // ── 行为记录梯度 ──
        printSeparator("行为记录异步化对比 (behaviorAsyncExecutor)");
        System.out.printf("%-10s %-14s %-14s %-10s %-10s%n", "记录数", "改造前", "改造后", "加速比", "延迟↓");
        printGradientDivider();
        for (int n : new int[]{1, 5, 20}) {
            long sSum = 0, pSum = 0;
            for (int r = 0; r < 10; r++) {
                long t0 = System.currentTimeMillis();
                for (int i = 0; i < n; i++) behaviorWriteSync();
                sSum += (System.currentTimeMillis() - t0);

                t0 = System.currentTimeMillis();
                AtomicInteger d = new AtomicInteger(0);
                for (int i = 0; i < n; i++)
                    behaviorExecutor.execute(() -> { behaviorWriteSync(); d.incrementAndGet(); });
                pSum += (System.currentTimeMillis() - t0);
            }
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            System.out.printf("%-10d %-14s %-14s %-10s %-10s%n",
                    n, fmt(avg(sSum, 10)), fmt(avg(pSum, 10)),
                    speedup(sSum, pSum), reduction(sSum, pSum));
        }

        // ── 并发请求吞吐对比 ──
        printSeparator("多用户并发搜索吞吐量");
        System.out.printf("%-16s %-14s %-14s %-14s%n", "并发用户", "改造前(候选/s)", "改造后(候选/s)", "吞吐提升");
        printGradientDivider();
        for (int u : new int[]{1, 3, 5, 10}) {
            long sSum = 0, pSum = 0;
            for (int r = 0; r < 10; r++) {
                sSum += runConcurrentSearchSerial(20, u);
                pSum += runConcurrentSearchParallel(20, u);
            }
            double sAvg = avg(sSum, 10);
            double pAvg = avg(pSum, 10);
            double sTp = u * 20 / (sAvg / 1000.0);
            double pTp = u * 20 / (pAvg / 1000.0);
            System.out.printf("%-16d %-14s %-14s %-14s%n",
                    u, fmtTp(sTp), fmtTp(pTp), fmtPct((pTp - sTp) / sTp * 100));
        }

        // ── 延迟分布 (P50/P95/P99) ──
        printSeparator("单个搜索请求延迟分布 (20候选人)");
        System.out.printf("%-10s %-14s %-14s %-10s%n", "分位数", "改造前", "改造后", "降低");
        printGradientDivider();
        List<Long> serLatencies = new ArrayList<>();
        List<Long> parLatencies = new ArrayList<>();
        for (int r = 0; r < 50; r++) {
            serLatencies.add(runSerial(buildCandidates(20)));
            parLatencies.add(runParallel(buildCandidates(20), matchExecutor));
        }
        Collections.sort(serLatencies);
        Collections.sort(parLatencies);
        for (String pct : new String[]{"P50", "P90", "P95", "P99"}) {
            double pctVal = Double.parseDouble(pct.substring(1)) / 100.0;
            int idx = (int) (serLatencies.size() * pctVal);
            long sVal = serLatencies.get(Math.min(idx, serLatencies.size() - 1));
            long pVal = parLatencies.get(Math.min(idx, parLatencies.size() - 1));
            System.out.printf("%-10s %-14s %-14s %-10s%n",
                    pct, fmt(sVal + "ms"), fmt(pVal + "ms"), fmtPct((sVal - pVal) * 100.0 / sVal));
        }

        // ── 线程池利用率 ──
        printSeparator("线程池运行统计");
        System.out.printf("  matchScoringExecutor  : 活跃线程=%d 完成任务=%d 队列峰值≈%d%n",
                matchExecutor.getActiveCount(), matchExecutor.getCompletedTaskCount(),
                matchExecutor.getQueue().size());
        System.out.printf("  cfComputeExecutor     : 活跃线程=%d 完成任务=%d%n",
                cfExecutor.getActiveCount(), cfExecutor.getCompletedTaskCount());
        System.out.printf("  behaviorAsyncExecutor : 活跃线程=%d 完成任务=%d 已完成=%d%n",
                behaviorExecutor.getActiveCount(), behaviorExecutor.getCompletedTaskCount(), done.get());
        System.out.println();

        matchExecutor.shutdown();
        cfExecutor.shutdown();
        behaviorExecutor.shutdown();
    }

    // ── 完整流水线 ──

    long runFullPipelineSerial(int candidates, int similarUsers, int behaviors) {
        long t0 = System.currentTimeMillis();
        // L1: content scoring
        List<Candidate> cands = buildCandidates(candidates);
        for (Candidate c : cands) c.score = scoringWork();
        // L2: CF
        for (long i = 0; i < similarUsers; i++) cfWork();
        // L3: behavior
        for (int i = 0; i < behaviors; i++) behaviorWriteSync();
        // L4: sort
        cands.sort(Comparator.comparingLong(c -> c.score));
        return System.currentTimeMillis() - t0;
    }

    long runFullPipelineParallel(int candidates, int similarUsers, int behaviors) {
        long t0 = System.currentTimeMillis();
        // L1: parallel content scoring
        List<Candidate> cands = buildCandidates(candidates);
        List<CompletableFuture<Void>> futures = cands.stream()
                .map(c -> CompletableFuture.runAsync(() -> c.score = scoringWork(), matchExecutor))
                .collect(Collectors.toList());
        // L2: parallel CF
        List<Long> cfIds = range(similarUsers);
        futures.addAll(cfIds.stream()
                .map(id -> CompletableFuture.runAsync(ConcurrencyBenchmarkTest::cfWork, cfExecutor))
                .collect(Collectors.toList()));
        // L3: async behavior
        for (int i = 0; i < behaviors; i++)
            behaviorExecutor.execute(ConcurrencyBenchmarkTest::behaviorWriteSync);
        // wait for L1 + L2 (L3 is fire-and-forget)
        futures.forEach(CompletableFuture::join);
        cands.sort(Comparator.comparingLong(c -> c.score));
        return System.currentTimeMillis() - t0;
    }

    long runConcurrentSearchSerial(int candidates, int concurrentUsers) {
        ExecutorService tomcat = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(concurrentUsers);
        List<Candidate> cands = buildCandidates(candidates);
        long t0 = System.currentTimeMillis();
        for (int u = 0; u < concurrentUsers; u++) {
            tomcat.submit(() -> { runSerial(cands); latch.countDown(); });
        }
        try { latch.await(); } catch (InterruptedException e) {}
        long elapsed = System.currentTimeMillis() - t0;
        tomcat.shutdown();
        return elapsed;
    }

    long runConcurrentSearchParallel(int candidates, int concurrentUsers) {
        ExecutorService tomcat = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(concurrentUsers);
        List<Candidate> cands = buildCandidates(candidates);
        long t0 = System.currentTimeMillis();
        for (int u = 0; u < concurrentUsers; u++) {
            tomcat.submit(() -> { runParallel(cands, matchExecutor); latch.countDown(); });
        }
        try { latch.await(); } catch (InterruptedException e) {}
        long elapsed = System.currentTimeMillis() - t0;
        tomcat.shutdown();
        return elapsed;
    }

    // ── 工作负载模拟 ──

    static long scoringWork() {
        try { Thread.sleep(2 + ThreadLocalRandom.current().nextInt(4)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        double acc = 0;
        for (int i = 0; i < 8000; i++) acc += Math.sin(i) * Math.cos(i * 0.7);
        for (int i = 0; i < 5000; i++) acc += Math.sqrt(i + acc) * 0.01;
        if (acc < 0) System.out.print("");
        return 1L;
    }

    static long cfWork() {
        try { Thread.sleep(5 + ThreadLocalRandom.current().nextInt(15)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        double acc = 0;
        for (int i = 0; i < 12000; i++) acc += Math.sin(i) * Math.cos(i * 0.5);
        for (int i = 0; i < 6000; i++) acc += Math.sqrt(Math.abs(i % 100 - acc * 0.01));
        if (acc < 0) System.out.print("");
        return 1L;
    }

    static void behaviorWriteSync() {
        try { Thread.sleep(3 + ThreadLocalRandom.current().nextInt(5)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ── helpers ──

    static class Candidate { final long id; volatile long score; Candidate(long id) { this.id = id; } }

    static List<Candidate> buildCandidates(int n) {
        List<Candidate> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(new Candidate(i));
        return list;
    }

    static List<Long> range(int n) {
        List<Long> list = new ArrayList<>(n);
        for (long i = 0; i < n; i++) list.add(i);
        return list;
    }

    long runSerial(List<Candidate> list) {
        long t0 = System.currentTimeMillis();
        for (Candidate c : list) c.score = scoringWork();
        return System.currentTimeMillis() - t0;
    }

    long runParallel(List<Candidate> list, Executor executor) {
        long t0 = System.currentTimeMillis();
        List<CompletableFuture<Void>> futures = list.stream()
                .map(c -> CompletableFuture.runAsync(() -> c.score = scoringWork(), executor))
                .collect(Collectors.toList());
        futures.forEach(CompletableFuture::join);
        return System.currentTimeMillis() - t0;
    }

    long runCFSerial(List<Long> ids) {
        long t0 = System.currentTimeMillis();
        for (Long id : ids) cfWork();
        return System.currentTimeMillis() - t0;
    }

    long runCFParallel(List<Long> ids, Executor executor) {
        long t0 = System.currentTimeMillis();
        List<CompletableFuture<Void>> futures = ids.stream()
                .map(id -> CompletableFuture.runAsync(ConcurrencyBenchmarkTest::cfWork, executor))
                .collect(Collectors.toList());
        futures.forEach(CompletableFuture::join);
        return System.currentTimeMillis() - t0;
    }

    // ── 格式化 ──

    static void printSeparator(String title) {
        System.out.println("\n" + "─".repeat(70));
        System.out.println("  " + title);
        System.out.println("─".repeat(70));
    }

    static void printDivider() { System.out.println("  " + "─".repeat(68)); }
    static void printGradientDivider() { System.out.println("  " + "─".repeat(56)); }

    static void printRow(String label, String name, String before, String after, String speedup, String reduction) {
        System.out.printf("  %-4s %-20s %-14s %-14s %-10s %-10s%n",
                label, name, before, after, speedup, reduction);
    }

    static double avg(long sum, int n) { return sum / (double) n; }
    static String fmt(double ms) { return String.format("%.1fms", ms); }
    static String fmt(String s) { return String.format("%-8s", s); }
    static String fmtX(double x) { return String.format("%.1fx", x); }
    static String fmtPct(double p) { return String.format("%.0f%%", p); }
    static String fmtTp(double tp) { return String.format("%.0f/s", tp); }
    static String speedup(long ser, long par) { return fmtX(ser / (double) par); }
    static String reduction(long ser, long par) { return fmtPct((ser - par) * 100.0 / ser); }
}
