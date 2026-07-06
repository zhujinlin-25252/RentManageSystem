import org.testng.annotations.Test;

public class PriorityTest {

    @Test(priority = 2)
    public void testMethodA() {
        System.out.println("执行 testMethodA (priority = 2)");
    }

    @Test(priority = 1)
    public void testMethodB() {
        System.out.println("执行 testMethodB (priority = 1)");
    }

    @Test(priority = 3)
    public void testMethodC() {
        System.out.println("执行 testMethodC (priority = 3)");
    }
}