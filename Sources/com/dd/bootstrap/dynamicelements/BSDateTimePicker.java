package com.dd.bootstrap.dynamicelements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components._private.ERXWOTextField;
import er.extensions.validation.ERXValidationException;

/**
 * Bootstrap date/time picker for WOTextField.
 * Uses https://eonasdan.github.io/bootstrap-datetimepicker.
 * Borrows heavily from ERXWOTextField and similarly implements its own takeValuesFromResponse.
 * Forces use of Java 8 LocalDate or LocalDateTime and will automatically use java.time.format.DateTimeFormatter.
 * If "formatter" binding is used it must be java.time.format.DateTimeFormatter.
 * If "dateFormat" binding is used a formatter will be instantiated in takeValuesFromResponse and the format String
 * must be compatible with java.time.format.DateTimeFormatter.
 * Has default values for "dateFormat" that are very American.
 * Cannot bind both "formatter" and "dateFormat"
 * By default, "dateonly" is true - in which case "value" should be bound to LocalDate object. Otherwise,
 * when false, "value" should be bound to a LocalDateTime object.
 * "dateonly" binding will control both the resulting calendar display and control the parsing in takeValuesFromResponse.
 * 
 * Contributions always welcome! Needs to be more flexible and customizeable. 
 * 
 * @author Tim Worman
 */

public class BSDateTimePicker extends ERXWOTextField {

  private WOAssociation _myId; //String
  private WOAssociation _label; //String
  private WOAssociation _placeholderText;
  private WOAssociation _dateOnly; //Boolean
  private WOAssociation _myClass;
  private NSMutableDictionary<String,WOAssociation> _associationsBackup;

  private static final String DATE_TIME_DEFAULT_FORMAT = "MM/dd/yyyy h:mm a";
  private static final String DATE_ONLY_DEFAULT_FORMAT = "MM/dd/yyyy";

  private static Logger log = LoggerFactory.getLogger(BSDateTimePicker.class);

  public BSDateTimePicker(String tagname, NSDictionary nsdictionary, WOElement woelement) {
    super(tagname, nsdictionary, woelement);
    _associationsBackup = nsdictionary.mutableClone();

    _value =            (WOAssociation)nsdictionary.remove("value");
    _myId =             (WOAssociation)nsdictionary.remove("id");
    _myClass =          (WOAssociation)nsdictionary.remove("class"); //honestly, not doing anything with class at this point
    _dateOnly =         (WOAssociation)nsdictionary.remove("dateonly");
    _dateFormat =       (WOAssociation)nsdictionary.remove("dateformat");
    _formatter =        (WOAssociation)nsdictionary.remove("formatter");
    _placeholderText =  (WOAssociation)nsdictionary.remove("placeholder");
    _numberFormat =     WOAssociation.associationWithValue(null); //always destroy any number formatting
    _useDecimalNumber = WOAssociation.associationWithValue(null); //always destroy any number formatting
    _id = null;
    _class = null;

    if(_dateFormat != null && _formatter != null) {
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> Cannot have 'dateFormat' and 'formatter' bound at the same time.");
    }
  }

  @Override
  public void appendAttributesToResponse(WOResponse response, WOContext context) {
    if(_placeholderText != null) {
      String phText = (String)_placeholderText.valueInComponent(context.component());
      if(phText.length() != 0) {
        response._appendTagAttributeAndValue("placeholder", phText, false);
      }
    }
    super.appendAttributesToResponse(response, context);
  }

  public void appendToResponse(WOResponse response, WOContext context) {

    //do some stuff here to wrap the input element
    String _idValue = divId(context);
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
    ERXResponseRewriter.addScriptCodeInHead(response, context, pickerJS(context));
  }

  private String pickerJS(WOContext context) {
    StringBuilder str = new StringBuilder();
    str.append("$(function () {$('#")
    .append(divId(context))
    .append("').datetimepicker(");
    if(dateOnly(context)) {
      str.append("{format: 'L'}");
    }
    str.append(");});");
    
    return str.toString();
  }

  private boolean dateOnly(WOContext context) {
    
    if(_dateOnly == null)
      return true;
    
    boolean noTime = _dateOnly.booleanValueInComponent(context.component());
    if(noTime)
      return true;

    return false;
  }

  @Override
  public void takeValuesFromRequest(WORequest worequest, WOContext context) {
    WOComponent component = context.component();
    DateTimeFormatter format = formatter(context);
    if(_formatter == null) {
      _formatter = WOAssociation.associationWithValue(format);
    }

    if(!isDisabledInContext(context) && context.wasFormSubmitted() && !isReadonlyInContext(context)) {
      String name = nameInContext(context, component);
      if(name != null) {
        String stringValue;
        boolean blankIsNull = _blankIsNull == null || _blankIsNull.booleanValueInComponent(component);
        if (blankIsNull) {
          stringValue = worequest.stringFormValueForKey(name);
        }
        else {
          Object objValue = worequest.formValueForKey(name);
          stringValue = (objValue == null) ? null : objValue.toString();
        }
        Object result = stringValue;
        
        if(stringValue != null) {
          if(format != null) {
            Object parsedObject = null;
            String reformattedObject = null;

            try {

              boolean useDateWithoutTime = dateOnly(context);
              if(useDateWithoutTime) {
                System.err.println("submitting date without time");
                parsedObject = LocalDate.parse(stringValue, format);
                reformattedObject = ((LocalDate)parsedObject).format(format);
                result = LocalDate.parse(reformattedObject, format);
              } else {
                System.err.println("submitting date with time");
                parsedObject = LocalDateTime.parse(stringValue, format);
                System.err.println("parsed object: " + parsedObject);
                reformattedObject = ((LocalDateTime)parsedObject).format(format);
                System.err.println("reformatted as string: " + reformattedObject);
                result = LocalDateTime.parse(reformattedObject, format);
                System.err.println("final result string: " + result);
              }
              //Object parsedObject = format.parseObject(stringValue);
              //String reformatedObject = format.format(parsedObject);
              //result = format.parseObject(reformatedObject);
            } catch(DateTimeParseException parseexception) {
              log.error("there was an exception parsing the date with formatter '{}'", format);
              String keyPath = _value.keyPath();
              ERXValidationException validationexception = new ERXValidationException(ERXValidationException.InvalidValueException, parseexception, keyPath, stringValue);
              component.validationFailedWithException(validationexception, stringValue, keyPath);
              return;
            }
          }
          
        } else if(blankIsNull && result.toString().length() == 0) {
          result = null;
        }
        System.err.println("value being set in date/time picker: " + result);
        _value.setValue(result, component);
      }

    }
  }

  protected void _appendValueAttributeToResponse(WOResponse response, WOContext context) {
    WOComponent component = context.component();
    Object objValue = _value.valueInComponent(component);
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

    log.info("getting the default formatter since one wasn't passed");
    DateTimeFormatter _default = DateTimeFormatter.ofPattern(dateFormat(context));
    log.info("formatter: {} - {}", _default.getClass(), _default.toString());

    return _default;
  }

  protected String valueInContext(WOContext context) {
    Object value = _value == null ? null : _value.valueInComponent(context.component());
    return value == null ? null : value.toString();
  }

  public String divId(WOContext context) {
    if(_myId == null) 
      return ERXWOContext.safeIdentifierName(context, false);

    return (String) _myId.valueInComponent(context.component());
  }
}