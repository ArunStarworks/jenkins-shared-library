
package com.company.selenium

enum browserversion {

Firefox("hello"), Chrome("hi"), None("none")

browserversion(String value)
{
this.value  = value
}

private final String value
public String value ()
{
return value
}
}