// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

/**
 * @since 5.3
 */
class ParameterTests extends TapestryCoreTestCase {

    /**
     * https://issues.apache.org/jira/browse/TAP5-1227
     */
    @Test
    void null_bound_to_primitive_field_is_an_error() {
        openLinks "Null Bound to Primitive Demo"

        assertTextPresent "Parameter 'value' of component NullBindingToPrimitive:showint is bound to null. This parameter is not allowed to be null."
    }

   /**
     * https://issues.apache.org/jira/browse/TAP5-1428
     */
    @Test
    void parameter_specified_with_component_annotation_must_match_a_formal_parameter() {
        openLinks "Unmatched Formal Parameter with @Component"

        assertTextPresent "Component InvalidFormalParameterDemo:counter does not include a formal parameter 'step' (and does not support informal parameters).",
                "Formal parameters", "end", "start", "value"

    }
}
