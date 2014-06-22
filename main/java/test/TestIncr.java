package test;

import scala.Int;

/**
 * Created by bjcheny on 6/13/14.
 */
public class TestIncr {
    private static void myincr(int i) {
        i++;
    }
    private static void newincr(Integer iObj) {
        iObj++;
    }
    public static void test() {
        int i = 10;
        myincr(i);
        System.out.println(i);

        Integer iObj = new Integer(i);
        newincr(iObj);
        System.out.println(iObj);
    }
}
