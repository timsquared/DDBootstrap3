package com.dd.bootstrap.dynamicelements;

import com.dd.bootstrap.components.BSComponent;
import com.dd.bootstrap.components.BSDynamicElement;
import com.dd.bootstrap.utils.ClientValidationSupport;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components._private.ERXWOTextField;

public class BSSlider extends ERXWOTextField {
  
  private WOAssociation _myId;
  private String _idAuto;
  private WOAssociation _label;   // String
  //private WOAssociation _required;  // boolean
  //private WOAssociation _helpText;  // String
  //private WOAssociation _unit;    // String
  private WOAssociation _myClass;
  //private WOAssociation _sliderId; //String
  private WOAssociation _slidermin; //
  private WOAssociation _slidermax;
  private WOAssociation _sliderstep;
  private WOAssociation _slidervalue;
  private NSMutableDictionary<String,WOAssociation> _associationsBackup;
  
    public BSSlider(String tagname, NSDictionary nsdictionary, WOElement woelement) {
        super(tagname, nsdictionary, woelement);
        _associationsBackup = nsdictionary.mutableClone();
        _myId = (WOAssociation) nsdictionary.remove("id");
        _id = null;
        _myClass = (WOAssociation) nsdictionary.remove("class");
        _class = null;
        
        //BSDynamicElementsHelper.AppendCSS(_associations, this);
        
        _label = _associations.removeObjectForKey("label");
       // _sliderId = _associations.removeObjectForKey("sliderId");
        _slidermin = _associations.removeObjectForKey("slidermin");
        _slidermax = _associations.removeObjectForKey("slidermax");
        _slidervalue = _associations.removeObjectForKey("slidervalue");
        _sliderstep = _associations.removeObjectForKey("sliderstep");
        //_required = _associations.removeObjectForKey("required");
        //_helpText = _associations.removeObjectForKey("helpText");
       // _unit = _associations.removeObjectForKey("unit");
    }
    
    @Override
    public void appendAttributesToResponse(WOResponse response, WOContext context) {
      super.appendAttributesToResponse(response, context);
      
      /*
      StringBuilder sb = new StringBuilder();
      sb.append("form-control slider");
      if (_myClass != null) {
        sb.append(" ").append((String) _myClass.valueInComponent(context.component()));
      }
      response._appendTagAttributeAndValue("class", sb.toString(), false);
      */
      
      response._appendTagAttributeAndValue("id", id(context), false);
      response._appendTagAttributeAndValue("data-slider-id", id(context) + "_slider", false);
      response._appendTagAttributeAndValue("data-slider-min", (String) _slidermin.valueInComponent(context.component()), false);
      response._appendTagAttributeAndValue("data-slider-max", (String) _slidermax.valueInComponent(context.component()), false);
      
      String step = _sliderstep == null ? "1" :
        (String) _sliderstep.valueInComponent(context.component());
      response._appendTagAttributeAndValue("data-slider-step", step, false);
      String startValue = _slidervalue == null ? "2" :
       String.valueOf(_slidervalue.valueInComponent(context.component()));
      response._appendTagAttributeAndValue("data-slider-value", startValue, false);
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
      BSDynamicElement.InjectCSSAndJS(response, context);
      super.appendToResponse(response, context);
      ERXResponseRewriter.addStylesheetResourceInHead(response, context, BSComponent.FRAMEWORK_NAME, "css/bootstrap-slider.css");
      ERXResponseRewriter.addScriptResourceInHead(response, context, BSComponent.FRAMEWORK_NAME, "js/bootstrap-slider.min.js");
      
      response.appendContentString(sliderJS(context));
    }
    
    private String sliderJS(WOContext context) {
      StringBuilder str = new StringBuilder();
      str.append("<script type=\"text/javascript\">\n");
      str.append("$('#")
      .append(id(context))
      .append("').slider({formatter: function(value) {")
      .append("return 'Current Value: ' + value;")
      .append("}});");
      str.append("\n</script>\n");
      return str.toString();
    }
    
    private String id(WOContext context) {
      
      if(_myId == null) 
        return ERXWOContext.safeIdentifierName(context, false);
      
      return (String) _myId.valueInComponent(context.component());
    }
}