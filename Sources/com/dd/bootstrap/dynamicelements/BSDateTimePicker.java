package com.dd.bootstrap.dynamicelements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dd.bootstrap.components.BSComponent;
import com.dd.bootstrap.components.BSDynamicElement;
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

  private WOAssociation _label; //String
  private WOAssociation _placeholderText;
  private WOAssociation _dateOnly; //Boolean
  private WOAssociation _timeOnly; //Boolean
  private WOAssociation _glyph; //String
  private WOAssociation _divId; //String
  private NSMutableDictionary<String,WOAssociation> _associationsBackup;
  
  private static final String GLYPH_LEFT                = "left";
  private static final String GLYPH_RIGHT               = "right";
  private static final String DATE_ONLY_DEFAULT_FORMAT  = "MM/dd/yyyy";
  private static final String TIME_ONLY_DEFAULT_FORMAT  = "h:mm a";
  private static final String DATE_TIME_DEFAULT_FORMAT  = DATE_ONLY_DEFAULT_FORMAT + " " + TIME_ONLY_DEFAULT_FORMAT;

  private static Logger log = LoggerFactory.getLogger(BSDateTimePicker.class);

  public BSDateTimePicker(String tagname, NSDictionary nsdictionary, WOElement woelement) {
    super("input", nsdictionary, woelement);
    BSDynamicElementsHelper.AppendCSS(_associations, this);
    _associationsBackup = nsdictionary.mutableClone();
    _divId =            _associationsBackup.objectForKey("id"); //we do this because we need the id even when we don't want the input element to have it
    
    _label =            _associations.removeObjectForKey("label");
    _dateOnly =         _associations.removeObjectForKey("dateonly");
    _timeOnly =         _associations.removeObjectForKey("timeonly");
    _glyph =            _associations.removeObjectForKey("glyph");
    _placeholderText =  _associations.removeObjectForKey("placeholder");
    _numberFormat =     WOAssociation.associationWithValue(null); //always destroy any number formatting
    _useDecimalNumber = WOAssociation.associationWithValue(null); //always destroy any number formatting

    if(_dateFormat != null && _formatter != null) {
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> Cannot have 'dateFormat' and 'formatter' bound at the same time.");
    }

    if(_dateOnly != null && _timeOnly != null) {
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> Cannot have 'timeonly' and 'dateonly' bound at the same time.");
    }
  }

  @Override
  public void appendAttributesToResponse(WOResponse response, WOContext context) {
    
    WOComponent component = context.component();

    if(_placeholderText != null) {
      String phText = (String)_placeholderText.valueInComponent(component);
      if(phText.length() != 0) {
        response._appendTagAttributeAndValue("placeholder", phText, false);
      }
    }
    
    super.appendAttributesToResponse(response, context);
  }

  public void appendToResponse(WOResponse response, WOContext context) {
    
    String _divIdIfNeeded = divIdInContext(context);
    String _glyphLocation = glyphInContext(context);
    String _labelString = labelInContext(context);
    boolean _glyphLeft = _glyphLocation != null && _glyphLocation.equals(GLYPH_LEFT);
    boolean _glyphRight = _glyphLocation != null && _glyphLocation.equals(GLYPH_RIGHT);
    boolean _timeOnly = timeOnlyInContext(context);
    boolean _hasGlyph = _glyphLeft ^ _glyphRight;
    boolean _hasLabel = _labelString != null && ! _labelString.equals("");
    
    //we need our boostrap components that are necesssary for date/time picker
    BSDynamicElement.InjectCSSAndJS(response, context);
    ERXResponseRewriter.addStylesheetResourceInHead(response, context, BSComponent.FRAMEWORK_NAME, "css/bootstrap-datetimepicker.min.css");
    ERXResponseRewriter.addStylesheetResourceInHead(response, context, BSComponent.FRAMEWORK_NAME, "prettify/prettify.css");
    ERXResponseRewriter.addScriptResourceInHead(response, context, BSComponent.FRAMEWORK_NAME, "js/moment.js");
    ERXResponseRewriter.addScriptResourceInHead(response, context, BSComponent.FRAMEWORK_NAME, "js/bootstrap-datetimepicker.min.js");
    ERXResponseRewriter.addScriptResourceInHead(response, context, BSComponent.FRAMEWORK_NAME, "prettify/run_prettify.js");
    
    //do some stuff here to wrap the input element
    StringBuilder sb = new StringBuilder();    
    if(_hasGlyph)
      sb.append("<div class=\"form-group\">"); //these div need to be here if there's a glyph
    
    if(_hasLabel) {
      sb.append("<label class=\"col-sm-2 control-label\">")
      .append(_labelString)
      .append("</label>");
    }
    
    if(_hasGlyph)
      sb.append("<div class=\"col-sm-4\">");//these div need to be here if there's a glyph
    
    sb.append("<div class=\"input-group\""); //append the normal outer div no matter what
    if(_hasGlyph)
      sb.append("id=" + _divIdIfNeeded); //this div needs to have the id if we're showing glyph
    
    sb.append(">"); //close the main div
    
    //if should show left-set glyphicon
    if(_glyphLeft) {
      insertGlyphHtml(sb, context);
    }
    
    response.appendContentString(sb.toString()); //append what we have so far
    super.appendToResponse(response, context); //generate our input element
    
    sb = new StringBuilder();
    if(_glyphRight) {
      insertGlyphHtml(sb, context);
    }
    
    if(_hasGlyph) {
      sb.append("</div></div>");
    }
    
    //finishing touches
    sb.append("</div>"); //closing div
    response.appendContentString(sb.toString());
    //append the script that provides our function to activate the date/time picker
    ERXResponseRewriter.addScriptCodeInHead(response, context, pickerJS(context));
  }

  private String pickerJS(WOContext context) {
    StringBuilder str = new StringBuilder();
    str.append("$(function () {$('#")
    .append(divIdInContext(context))
    .append("').datetimepicker(");
    if(dateOnlyInContext(context)) {
      log.debug("the content javascript is appending for date only");
      str.append("{format: 'L'}");
    } else if(timeOnlyInContext(context)) {
      log.debug("the content javascript is appending for time only");
      str.append("{format: 'LT'}");
    }
    str.append(");});");

    return str.toString();
  }
  
  private StringBuilder insertGlyphHtml(StringBuilder sb, WOContext context) {
    
    StringBuilder _seed = sb;
    _seed.append("<span class=\"input-group-addon\">");
    if(timeOnlyInContext(context)) {
      sb.append("<span class=\"glyphicon glyphicon-time\">");
    } else {
      sb.append("<span class=\"glyphicon glyphicon-calendar\">");
    }
    sb.append("</span></span>");
    
    return sb;
  }
  
  @Override
  public String idInContext(WOContext context) {
    
    String _theId = super.idInContext(context);
    
    if(glyphInContext(context) == null)
      return _theId;
    
    //we do this because when using glyph the id should be attached to surrounding div
    //so the input element should not have that id!!
    return "";
  }
  
  public String divIdInContext(WOContext context) {
    
    if(_divId == null)
      return null;
    
    return (String)_divId.valueInComponent(context.component());
  }

  private boolean dateOnlyInContext(WOContext context) {

    if(_dateOnly == null)
      return false;

    boolean noTime = _dateOnly.booleanValueInComponent(context.component());
    if(noTime)
      return true;

    return false;
  }

  private boolean timeOnlyInContext(WOContext context) {

    if(_timeOnly == null)
      return false;

    boolean noDate = _timeOnly.booleanValueInComponent(context.component());
    if(noDate)
      return true;

    return false;
  }
  
  private String glyphInContext(WOContext context) {

    if(_glyph == null)
      return null;

    String glyph = (String)_glyph.valueInComponent(context.component());
    return glyph;
  }
  
  private String labelInContext(WOContext context) {
    if(_label == null)
      return null;
    
    String label = (String)_label.valueInComponent(context.component());
    return label;
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

              boolean useDateWithoutTime = dateOnlyInContext(context);
              boolean useTimeWithoutDate = timeOnlyInContext(context);

              if(useDateWithoutTime) {
                log.debug("submitting date with no time");
                parsedObject = LocalDate.parse(stringValue, format);
                reformattedObject = ((LocalDate)parsedObject).format(format);
                result = LocalDate.parse(reformattedObject, format);
              } else if(useTimeWithoutDate) {  
                log.debug("submitting the time with no date");
                parsedObject = LocalTime.parse(stringValue, format);
                log.debug("parsed object '{}'", parsedObject);
                reformattedObject = ((LocalTime)parsedObject).format(format);
                log.debug("reformatted as string: '{}'", reformattedObject);
                result = LocalTime.parse(reformattedObject, format);
                log.debug("final result string: '{}'", result);
              } else {
                log.debug("submitting date with time");
                parsedObject = LocalDateTime.parse(stringValue, format);
                log.debug("parsed object '{}'", parsedObject);
                reformattedObject = ((LocalDateTime)parsedObject).format(format);
                log.debug("reformatted as string: '{}'", reformattedObject);
                result = LocalDateTime.parse(reformattedObject, format);
                log.debug("final result string: '{}'", result);
              }
              //Object parsedObject = format.parseObject(stringValue);
              //String reformatedObject = format.format(parsedObject);
              //result = format.parseObject(reformatedObject);
            } catch(DateTimeParseException parseexception) {
              log.error("there was an exception parsing the date with formatter");
              log.error("the string is '{}' and the formatter is using the format string '{'}", stringValue, dateFormat(context));
              String keyPath = _value.keyPath();
              ERXValidationException validationexception = new ERXValidationException(ERXValidationException.InvalidValueException, parseexception, keyPath, stringValue);
              component.validationFailedWithException(validationexception, stringValue, keyPath);
              return;
            }

          } else if(blankIsNull && result.toString().length() == 0) {
            result = null;
          }
        }

        log.debug("value being set in date/time picker: " + result);
        _value.setValue(result, component);
      }
    }
  }

  protected void _appendValueAttributeToResponse(WOResponse response, WOContext context) {
    WOComponent component = context.component();
    Object objValue = _value.valueInComponent(component);
    log.debug("object value in component: '{}'", objValue);
    if (objValue != null) {
      String strValue = null;
      DateTimeFormatter format = formatter(context);
      if (format != null) {
        Object parsedValue = null;
        String formattedStrValue = null;
        try {
          if(dateOnlyInContext(context)) {
            formattedStrValue = ((LocalDate)objValue).format(format);
            parsedValue = LocalDate.parse(formattedStrValue, format);
            strValue = ((LocalDate)parsedValue).format(format);
          } else if(timeOnlyInContext(context)) {
            formattedStrValue = ((LocalTime)objValue).format(format);
            parsedValue = LocalTime.parse(formattedStrValue, format);
            strValue = ((LocalTime)parsedValue).format(format);
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
        
        log.debug("formatted string value: '{}'", formattedStrValue);
        log.debug("parsed value and class: '{}', {}", parsedValue, parsedValue.getClass());
        log.debug("string value: '{}'", strValue);
      }
      
      if (strValue == null) {
        strValue = objValue.toString();
      }
      response._appendTagAttributeAndValue("value", strValue, true);
    }
    if (isReadonlyInContext(context)) {
      response._appendTagAttributeAndValue("readonly", "readonly", false);
    }
  }

  private String dateFormat(WOContext context) {
    if(_dateFormat != null)
      return (String)_dateFormat.valueInComponent(context.component());
    
    if(timeOnlyInContext(context)) {
      return TIME_ONLY_DEFAULT_FORMAT;
    } else if(dateOnlyInContext(context)) {
      return DATE_ONLY_DEFAULT_FORMAT;
    }
    log.debug("not using date-only or time only in picker");
    return DATE_TIME_DEFAULT_FORMAT;
  }

  public synchronized DateTimeFormatter formatter(WOContext context) {
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
}