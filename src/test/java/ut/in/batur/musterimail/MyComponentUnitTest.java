package ut.in.batur.musterimail;

import org.junit.Test;
import in.batur.musterimail.api.MyPluginComponent;
import in.batur.musterimail.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}