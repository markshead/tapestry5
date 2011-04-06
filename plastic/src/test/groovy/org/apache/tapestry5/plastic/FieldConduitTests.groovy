// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.plastic

import org.apache.tapestry5.plastic.FieldConduit;
import org.apache.tapestry5.plastic.PlasticManager;

import spock.lang.Specification 

class FieldConduitTests extends Specification
{
    def mgr = new PlasticManager()
    
    def "setting a field invokes the conduit"() {
        
        FieldConduit fc = Mock()
        
        def pc = mgr.getPlasticClass("testsubjects.IntFieldHolder")
        
        pc.allFields.first().setConduit(fc)
        
        def o = pc.createInstantiator().newInstance()
        
        when:
        
        o.setValue(123)
        
        then:
        
        1 * fc.set(_, 123)     
        
        when:
        fc.get(_) >>> 999
        
        then:
        
        o.getValue() == 999
        
        expect:
        
        // The field doesn't really get used (it may be removed some day, though its useful because of the
        // annotations that may be on it).
        
        o.@value == 0        
    }
    
    def "field initializations are visible to the conduit"() {         
        FieldConduit fc = Mock()
        
        def pc = mgr.getPlasticClass("testsubjects.LongFieldHolder")
        
        pc.allFields.first().setConduit(fc)
        
        when:
        
        pc.createInstantiator().newInstance()
        
        then:
        
        // 100 is the initial value of the field
        
        1 * fc.set(_, 100)
    }
}