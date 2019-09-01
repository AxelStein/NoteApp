package com.axel_stein.noteapp;

import org.junit.Before;
import org.junit.Test;

public class DataManagerTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void test() {
        String s = "test";
        String q = "est";

        System.out.println("TAG result = " + String.valueOf(s.indexOf(q)));
    }

}