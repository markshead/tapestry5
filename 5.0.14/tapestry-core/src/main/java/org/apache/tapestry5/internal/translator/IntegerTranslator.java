// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.translator;

import org.apache.tapestry5.Translator;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.ioc.Messages;

/**
 * A translator for type integer.
 */
public final class IntegerTranslator implements Translator<Integer>
{
    public Class<Integer> getType()
    {
        return Integer.class;
    }

    /**
     * Parses blank values to null, otherwise parses the client value to an integer.
     *
     * @throws ValidationException if the clientValue can not be parsed as an integer
     */
    public Integer parseClient(String clientValue, Messages messages) throws ValidationException
    {

        try
        {
            return new Integer(clientValue.trim());
        }
        catch (NumberFormatException ex)
        {
            throw new ValidationException(messages.format("integer-format-exception", clientValue));
        }
    }

    /**
     * Converts null to the blank string, non-null to a string representation.
     */
    public String toClient(Integer value)
    {
        return value.toString();
    }

}