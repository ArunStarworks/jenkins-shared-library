
package com.company.selenium

enum Browserversion {

Firefox("hello"), Chrome("hi"), None("none")

Browserversion(String value)
{
this.value  = value
}

private final String value
public String value ()
{
return value
}
}