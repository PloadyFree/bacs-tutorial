package istu.bacs.externalapi.fake;

import istu.bacs.db.submission.Submission;
import istu.bacs.db.submission.SubmissionResult;
import istu.bacs.db.submission.Verdict;

import java.util.Random;

import static istu.bacs.db.submission.Verdict.*;
import static istu.bacs.externalapi.fake.Measure.MEGABYTE;
import static istu.bacs.externalapi.fake.Measure.MILLISECOND;

class SubmissionResultUpdater {

    private final Random rnd = new Random();

    void updateSubmissionResult(Submission submission) {
        String solution = submission.getSolution();

        if (solution.equalsIgnoreCase(ACCEPTED.name())) {
            accepted(submission);
        } else if (solution.equalsIgnoreCase(TIME_LIMIT_EXCEEDED.name())) {
            timeLimit(submission);
        } else if (solution.equalsIgnoreCase(MEMORY_LIMIT_EXCEEDED.name())) {
            memoryLimit(submission);
        } else {
            Verdict verdict = tryFindVerdict(solution);
            if (verdict == null || verdict == COMPILE_ERROR) compileError(submission);
            else setVerdict(submission, verdict);
        }
    }

    private void accepted(Submission submission) {
        SubmissionResult result = submission.getResult();

        result.setVerdict(ACCEPTED);
        result.setTimeUsedMillis(rnd.nextInt(submission.getProblem().getTimeLimitMillis() + 1));
        result.setMemoryUsedBytes(rnd.nextInt(submission.getProblem().getMemoryLimitBytes() + 1));
    }

    private void timeLimit(Submission submission) {
        SubmissionResult result = submission.getResult();

        result.setVerdict(TIME_LIMIT_EXCEEDED);
        result.setTestsPassed(rnd.nextInt(100));
        result.setTimeUsedMillis(submission.getProblem().getTimeLimitMillis() + (rnd.nextInt(100) + 1) * MILLISECOND);
        result.setMemoryUsedBytes(rnd.nextInt(submission.getProblem().getTimeLimitMillis() + 1));
    }

    private void memoryLimit(Submission submission) {
        SubmissionResult result = submission.getResult();

        result.setVerdict(MEMORY_LIMIT_EXCEEDED);
        result.setTestsPassed(rnd.nextInt(100));
        result.setTimeUsedMillis(rnd.nextInt(submission.getProblem().getTimeLimitMillis() + 1));
        result.setMemoryUsedBytes(submission.getProblem().getMemoryLimitBytes() + (rnd.nextInt(100) + 1) * MEGABYTE);
    }

    private void compileError(Submission submission) {
        SubmissionResult result = submission.getResult();

        result.setVerdict(COMPILE_ERROR);
        result.setBuildInfo("Something is wrong");
    }

    private void setVerdict(Submission submission, Verdict verdict) {
        SubmissionResult result = submission.getResult();

        result.setVerdict(verdict);
        result.setTestsPassed(rnd.nextInt(100));
        result.setTimeUsedMillis(rnd.nextInt(submission.getProblem().getTimeLimitMillis() + 1));
        result.setMemoryUsedBytes(rnd.nextInt(submission.getProblem().getMemoryLimitBytes() + 1));
    }

    private Verdict tryFindVerdict(String solution) {
        for (Verdict verdict : Verdict.values())
            if (solution.equalsIgnoreCase(verdict.name()))
                return verdict;
        return null;
    }
}