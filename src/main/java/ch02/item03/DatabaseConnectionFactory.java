package ch02.item03;

public final class DatabaseConnectionFactory {

    private static final DatabaseConnectionFactory INSTANCE = new DatabaseConnectionFactory();

    private DatabaseConnectionFactory() {
    }

    public static DatabaseConnectionFactory getInstance() {
        return INSTANCE;
    }

    public void query(String sql) {
        System.out.println("[factory] execute: " + sql);
    }
}