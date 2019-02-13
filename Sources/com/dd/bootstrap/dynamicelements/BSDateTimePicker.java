package com.dd.bootstrap.dynamicelements;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.dd.bootstrap.components.BSComponent;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSValidation.ValidationException;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.appserver.ERXWOContext;

public class BSDateTimePicker extends BSTextField {
  
  private WOAssociation _valueAssociation;
  private WOAssociation _myId; //String
  private WOAssociation _label; //String
  private WOAssociation _placeholderText;
  //private WOAssociation _myFormat; //String
  //private WOAssociation _myFormatter; //String
  private WOAssociation _dateOnly; //Boolean
  private WOAssociation _myClass;
  
  private String _id;
  private String _name;
  private String _dateTimeString;
  private NSMutableDictionary<String,WOAssociation> _associationsBackup;
  
  private static final String DATE_TIME_DEFAULT_FORMAT = "MM/dd/yyyy h:mm a";
  private static final String DATE_ONLY_DEFAULT_FORMAT = "MM/dd/yyyy";
  
    public BSDateTimePicker(String tagname, NSDictionary nsdictionary, WOElement woelement) {
      super(tagname, nsdictionary, woelement);
      _associationsBackup = nsdictionary.mutableClone();
      
      _valueAssociation = (WOAssociation)nsdictionary.remove("value");
      _myId = (WOAssociation) nsdictionary.remove("id");
      _id = null;
      _myClass = (WOAssociation) nsdictionary.remove("class");
      _class = null;
      _dateOnly = (WOAssociation)nsdictionary.remove("dateonly");
      _dateFormat = (WOAssociation)nsdictionary.remove("dateformat");
      //_dateFormat = null;
      _formatter = (WOAssociation)nsdictionary.remove("formatter");
      _placeholderText = (WOAssociation)nsdictionary.remove("placeholder");
      _numberFormat = WOAssociation.associationWithValue(null); //always destroy any number formatting
      
      if(_dateFormat != null && _formatter != null) {
        throw new WODynamicElementCreationException("<" + getClass().getName() + "> Cannot have 'dateFormat' and 'formatter' attributes at the same time.");
      }
    }
    
    @Override
    public void appendAttributesToResponse(WOResponse response, WOContext context) {
      super.appendAttributesToResponse(response, context);
      if(_placeholderText != null) {
        String phText = (String)_placeholderText.valueInComponent(context.component());
        if(phText.length() != 0) {
          response._appendTagAttributeAndValue("placeholder", phText, false);
        }
      }
    }
    
    public void appendToResponse(WOResponse response, WOContext context) {
      
      //do some stuff here to wrap the input element
      String _idValue = id(context);
      StringBuilder sb = new StringBuilder();
      sb.append("<div class=\"input-group\" id=")
      .append(_idValue)
      .append("><span class=\"input-group-addon\">")
      .append("<span class=\"glyphicon glyphicon-calendar\"/></span>");
      response.appendContentString(sb.toString());
      
      super.appendToResponse(response, context); //generate our input element
      _appendValueAttributeToResponse(response, context);
      response.appendContentString("</div>"); //close surrounding div after input
      
    //we need our boostrap components that are necesssary for date/time picker
      ERXResponseRewriter.addStylesheetResourceInHead(response, context, BSComponent.FRAMEWORK_NAME, "css/bootstrap-datetimepicker.min.css");
      ERXResponseRewriter.addStylesheetResourceInHead(response, context, BSComponent.FRAMEWORK_NAME, "prettify/prettify.css");
      ERXResponseRewriter.addScriptResourceInHead(response, context, BSComponent.FRAMEWORK_NAME, "js/moment.js");
      ERXResponseRewriter.addScriptResourceInHead(response, context, BSComponent.FRAMEWORK_NAME, "js/bootstrap-datetimepicker.min.js");
      ERXResponseRewriter.addScriptResourceInHead(response, context, BSComponent.FRAMEWORK_NAME, "prettify/run_prettify.js");
      
      //do some stuff here to finish wrapping the input element
      
      //append the script that provides our function to activate the date/time picker
      response.appendContentString(pickerJS(context));
    }
    
    private String pickerJS(WOContext context) {
      StringBuilder str = new StringBuilder();
      str.append("jQuery(function () {jQuery('#")
      .append(id(context))
      .append("').datetimepicker();})");
      //.append("})");
      return str.toString();
    }
    
    private boolean dateOnly(WOContext context) {
      //the default is to be date-only and not show time controls
      if(_dateOnly == null || ! _dateOnly.booleanValueInComponent(context.component()))
        return true;
      
      return false;
    }
    
    @Override
    public void takeValuesFromRequest(WORequest worequest, WOContext context) {
      WOComponent component = context.component();
      String strValue = (String)_valueAssociation.valueInComponent(context.component());
      Object objValue = strValue;
      if (strValue != null && strValue.length() != 0) {
        DateTimeFormatter format = formatter(context);
        
        if (format != null) {
          Object parsedValue = null;
          String text = null;
          
          try {
            if(dateOnly(context)) {
              parsedValue = LocalDate.parse(strValue, format);
              text = ((LocalDate)parsedValue).format(format);
              objValue = LocalDate.parse(text, format);
            } else {
              parsedValue = LocalDateTime.parse(strValue, format);
              text = ((LocalDateTime)parsedValue).format(format);
              objValue = LocalDateTime.parse(text, format);
            }
          }
          catch (DateTimeParseException parseexception) {
            String valueKeyPath = _valueAssociation.keyPath();
            ValidationException validationexception = new ValidationException(parseexception.getMessage(), strValue, valueKeyPath);
            component.validationFailedWithException(validationexception, strValue, valueKeyPath);
          }
          if (objValue != null && _useDecimalNumber != null && _useDecimalNumber.booleanValueInComponent(component)) {
            objValue = new BigDecimal(objValue.toString());
          }
        }
        else if (objValue.toString().length() == 0) {
          objValue = null;
        }
      }

      _valueAssociation.setValue(objValue, component);
    }
    
    protected void _appendValueAttributeToResponse(WOResponse response, WOContext context) {
      WOComponent component = context.component();
      Object objValue = _valueAssociation.valueInComponent(component);
      if (objValue != null) {
        String strValue = null;
        DateTimeFormatter format = formatter(context);
        if (format != null) {
          Object parsedValue = null;
          String formattedStrValue = null;
          try {
            if(dateOnly(context)) {
              formattedStrValue = ((LocalDate)objValue).format(format);
              parsedValue = LocalDate.parse(formattedStrValue, format);
              strValue = ((LocalDate)parsedValue).format(format);
            } else {
              formattedStrValue = ((LocalDateTime)objValue).format(format);
              parsedValue = LocalDateTime.parse(formattedStrValue, format);
              strValue = ((LocalDateTime)parsedValue).format(format);
            }
          }
          catch (IllegalArgumentException illegalargumentexception) {
            NSLog._conditionallyLogPrivateException(illegalargumentexception);
          }
          catch (DateTimeParseException parseexception) {
            NSLog._conditionallyLogPrivateException(parseexception);
          }
        }
        if (strValue == null) {
          strValue = objValue.toString();
        }
        if (_escapeHTML != null && _escapeHTML.booleanValueInComponent(component)) {
          response.appendContentHTMLString(strValue);
        }
        else {
          response.appendContentString(strValue);
        }
      }
    }
    
    private String dateFormat(WOContext context) {
      if(_dateFormat != null)
        return (String)_dateFormat.valueInComponent(context.component());
      
      if(dateOnly(context)) {
        return DATE_ONLY_DEFAULT_FORMAT;
      }
      System.err.println("not using date-only in picker");
      return DATE_TIME_DEFAULT_FORMAT;
    }
    
    public DateTimeFormatter formatter(WOContext context) {
      //preferred - formatter is not null
      if(_formatter != null) {
        DateTimeFormatter _theFormatter = null;
        try {
        _theFormatter = (DateTimeFormatter)_formatter.valueInComponent(context.component());
        } catch (ClassCastException c) {
          throw c;
        }
        return _theFormatter;
      }
      
      return DateTimeFormatter.ofPattern(dateFormat(context));
    }
    
    private String id(WOContext context) {
      if(_myId == null) 
        return ERXWOContext.safeIdentifierName(context, false);
      
      return (String) _myId.valueInComponent(context.component());
    }
}