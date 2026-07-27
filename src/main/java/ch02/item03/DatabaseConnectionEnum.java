package ch02.item03;

public enum DatabaseConnectionEnum {

    INSTANCE;

    private int queryCount = 0;

    public void query(String sql) {
        queryCount++;
        System.out.println("[enum#" + queryCount + "] execute: " + sql);
    }

    public int getQueryCount() {
        return queryCount;
    }
}