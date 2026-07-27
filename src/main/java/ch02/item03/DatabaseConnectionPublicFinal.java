package ch02.item03;

public final class DatabaseConnectionPublicFinal {

    public static final DatabaseConnectionPublicFinal INSTANCE = new DatabaseConnectionPublicFinal();

    private DatabaseConnectionPublicFinal() {
    }

    public void query(String sql) {
        System.out.println("[public-final] execute: " + sql);
    }
}