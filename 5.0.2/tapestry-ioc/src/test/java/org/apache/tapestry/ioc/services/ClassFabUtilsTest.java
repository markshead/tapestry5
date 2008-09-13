// Copyright 2006 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.ioc.services;

import org.apache.tapestry.ioc.test.IOCTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ClassFabUtilsTest extends IOCTestCase
{

    @Test(dataProvider = "provider")
    public void get_jvm_classname(String input, String expected)
    {
        String actual = ClassFabUtils.getJVMClassName(input);

        assertEquals(actual, expected);
    }

    @DataProvider(name = "provider")
    public Object[][] createInputs()
    {
        return new Object[][]
        {
        { "java.lang.Object", "java.lang.Object" },
        { "int", "int" },
        { "int[]", "[I" },
        { "java.lang.Throwable[]", "[Ljava.lang.Throwable;" },
        { "byte[][]", "[[B" },
        { "java.lang.Runnable[][]", "[[Ljava.lang.Runnable;" } };
    }

    @Test(dataProvider = "typeCodeProvider")
    public void get_type_code(Class input, String expected)
    {
        assertEquals(ClassFabUtils.getTypeCode(input), expected);
    }

    @DataProvider(name = "typeCodeProvider")
    public Object[][] get_type_code_provider()
    {
        return new Object[][]
        {
        { int.class, "I" },
        { int[].class, "[I" },
        { Thread.class, "Ljava/lang/Thread;" },
        { Thread[].class, "[Ljava/lang/Thread;" },
        { Double[][].class, "[[Ljava/lang/Double;" },
        { void.class, "V" },

        };
    }

    @Test
    public void unwrap_method()
    {
        assertEquals(ClassFabUtils.getUnwrapMethodName("int"), "intValue");
    }

    @Test
    public void wrapper_type_name()
    {
        assertEquals(ClassFabUtils.getWrapperTypeName("int"), "java.lang.Integer");
    }

    @Test
    public void wrapper_type()
    {
        assertEquals(ClassFabUtils.getWrapperType(int.class), Integer.class);
        assertEquals(ClassFabUtils.getWrapperType(getClass()), getClass());
    }
}