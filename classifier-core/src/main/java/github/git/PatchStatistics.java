package github.git;


public class PatchStatistics {

    private int additions;

    private int modifications;

    private int deletions;

    public PatchStatistics(int additions, int modifications, int deletions) {
        this.additions = additions;
        this.modifications = modifications;
        this.deletions = deletions;
    }

    public int getAdditions() {
        return additions;
    }

    public int getModifications() {
        return modifications;
    }

    public int getDeletions() {
        return deletions;
    }

}
