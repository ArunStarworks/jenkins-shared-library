#!/usr/bin/groovy

package com.company.selenium

enum browserversion {

Firefox("firefox"), Chrome("chrome"), None("none")

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