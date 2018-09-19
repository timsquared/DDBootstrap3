package com.dd.bootstrap.components;

import java.time.LocalDateTime;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.appserver.ERXResponseRewriter;

public class BSDateTimePicker extends BSComponent {
  
    public BSDateTimePicker(WOContext context) {
        super(context);
    }
    
    public boolean isStateless() {
      return true;
    }
    
    @Override
    protected void injectCustomHeadData(WOResponse response, WOContext context) {
      ERXResponseRewriter.addStylesheetResourceInHead(response, context, FRAMEWORK_NAME, "css/bootstrap-datetimepicker.min.css");
      ERXResponseRewriter.addStylesheetResourceInHead(response, context, FRAMEWORK_NAME, "prettify/prettify.css");
      ERXResponseRewriter.addScriptResourceInHead(response, context, FRAMEWORK_NAME, "js/moment.js");
      ERXResponseRewriter.addScriptResourceInHead(response, context, FRAMEWORK_NAME, "js/bootstrap-datetimepicker.min.js");
      ERXResponseRewriter.addScriptResourceInHead(response, context, FRAMEWORK_NAME, "prettify/run_prettify.js");
    }
    
    public String datetime() {
      String theDate = (String)valueForBinding("datetime");
      System.err.println("this is the date being passed to the date component: " + theDate);
      return theDate;
    }
    
    public void setDatetime(String newDate) {
      setValueForBinding(newDate, "datetime");
    }
}