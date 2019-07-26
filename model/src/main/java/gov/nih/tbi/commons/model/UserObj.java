
package gov.nih.tbi.commons.model;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface UserObj
{

    String name();
}
