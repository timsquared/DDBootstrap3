package com.dd.bootstrap.dynamicelements;

import com.dd.bootstrap.utils.ClientValidationSupport;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.components._private.ERXWOTextField;

public class BSSlider extends BSEditorRowTextField {
  
  private WOAssociation _label;   // String
  private WOAssociation _required;  // boolean
  private WOAssociation _helpText;  // String
  private WOAssociation _unit;    // String
  private WOAssociation _myClass;
  private WOAssociation _sliderId; //String
  private WOAssociation _slidermin; //
  private WOAssociation _slidermax;
  private WOAssociation _slidervalue;
  private NSMutableDictionary<String,WOAssociation> _associationsBackup;
  
    public BSSlider(String tagname, NSDictionary nsdictionary, WOElement woelement) {
        super(tagname, nsdictionary, woelement);
        _associationsBackup = nsdictionary.mutableClone();
        _myClass = (WOAssociation) nsdictionary.remove("class");
        _class = null;
        
        //BSDynamicElementsHelper.AppendCSS(_associations, this);
        
        _label = _associations.removeObjectForKey("label");
        _required = _associations.removeObjectForKey("required");
        _helpText = _associations.removeObjectForKey("helpText");
        _unit = _associations.removeObjectForKey("unit");
    }
    
    @Override
    public void appendAttributesToResponse(WOResponse response, WOContext context) {
      super.appendAttributesToResponse(response, context);
      
      StringBuilder sb = new StringBuilder();
      /*
      sb.append("form-control");
      if (_myClass != null) {
        sb.append(" ").append((String) _myClass.valueInComponent(context.component()));
      }
      response._appendTagAttributeAndValue("class", sb.toString(), false);
      ClientValidationSupport.appendValidiationBindings(_associationsBackup, response, context);
      */
      
      
    }
}