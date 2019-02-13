package com.dd.bootstrap.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.appserver.ERXWOContext;

public class BSDateTimePicker extends BSComponent {
  
  private String _id;
  private String _dateTimeString;
  private static DateTimeFormatter _formatter = null;
  private static final String _dateTimeFormat = "MM/dd/yyyy h:mm a";
  
    public BSDateTimePicker(WOContext context) {
        super(context);
    }
    
    public void appendToResponse(WOResponse response, WOContext context) {
      super.appendToResponse(response, context);
      ERXResponseRewriter.addScriptCodeInHead(response, context, pickerJS(context));
    }
    
    public boolean isStateless() {
      return true;
    }
    
    private String pickerJS(WOContext context) {
      StringBuilder str = new StringBuilder();
      str.append("jQuery(function () {jQuery('#")
      .append(id())
      .append("').datetimepicker();})");
      //.append("})");
      return str.toString();
    }
    
    @Override
    protected void injectCustomHeadData(WOResponse response, WOContext context) {
      ERXResponseRewriter.addStylesheetResourceInHead(response, context, FRAMEWORK_NAME, "css/bootstrap-datetimepicker.min.css");
      ERXResponseRewriter.addStylesheetResourceInHead(response, context, FRAMEWORK_NAME, "prettify/prettify.css");
      ERXResponseRewriter.addScriptResourceInHead(response, context, FRAMEWORK_NAME, "js/moment.js");
      ERXResponseRewriter.addScriptResourceInHead(response, context, FRAMEWORK_NAME, "js/bootstrap-datetimepicker.min.js");
      ERXResponseRewriter.addScriptResourceInHead(response, context, FRAMEWORK_NAME, "prettify/run_prettify.js");
    }
    
    public String label() {
      return stringValueForBinding("label", "Date");
    }
    
    public void setLabel(String newLabel) {
      //do nothing
    }
    
    public String inputString() {
      if(datetime() != null)
        return datetime().format(formatter());
      
      return null;
    }
    
    public void setInputString(String newInputString) {
      setDatetime(LocalDateTime.parse(newInputString, formatter()));
    }
    
    public DateTimeFormatter formatter() {
      if(_formatter == null)
        _formatter = DateTimeFormatter.ofPattern(_dateTimeFormat);
      return _formatter;
    }
    
    public LocalDateTime datetime() {
      LocalDateTime theDate = (LocalDateTime)valueForBinding("datetime");
 
      return theDate;
    }
    
    public void setDatetime(LocalDateTime newDate) {
      setValueForBinding(newDate, "datetime");
    }
    
    public String id() {
      if(_id == null) {
        _id = stringValueForBinding("id", null);
        
        if(_id == null) {
          _id = ERXWOContext.safeIdentifierName(context(), false);
        }
      }
      return _id;
    }
    
    public void setId(String newId) {
      //do nothing
    }
}