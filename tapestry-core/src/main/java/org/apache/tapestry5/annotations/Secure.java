// Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.annotations;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.annotations.UseWith;
import org.apache.tapestry5.ioc.annotations.AnnotationUseContext;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * A marker annotation that indicates that the page in question may only be accessed via HTTPS.
 * <p>
 * Normally, this annotation is ignored in development mode and only used in production mode. This can be changed
 * via the {@link SymbolConstants#SECURE_ENABLED} configuration symbol. 
 *
 * @see org.apache.tapestry5.MetaDataConstants#SECURE_PAGE
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@UseWith(AnnotationUseContext.PAGE)
public @interface Secure
{
}
