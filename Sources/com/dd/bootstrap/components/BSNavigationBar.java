package com.dd.bootstrap.components;

import org.apache.commons.lang3.StringUtils;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXStringUtilities;

/**
 * BSNavigationBar
 * 
 * <b>Bindings:</b>
 * Property		Required		Type
 * title						String
 * list			yes 			NSArray<BSNavigationBar.Item>
 * item 		yes 			BSNavigationBar.Item
 * defaultItem 	yes	 			BSNavigationBar.Item
 * delegate 					BSNavigationBar.Delegate
 * 
 * @author robin
 *
 */
public class BSNavigationBar extends BSComponent {
	
	/**
	 * Class Item used in BSNavigationBar
	 * @author robin
	 *
	 */
	public static class Item {
		public String title;
		public Object tag;
		public NSMutableArray<Item> childItems = new NSMutableArray<Item>();
		public Item() {}
		public Item(String title) { this.title = title; }
		public Item(String title, Object tag) { this.title = title; this.tag = tag; }
		public WOActionResults action(WOContext context) { return null; }
		public String href;
		public String hrefTarget;
		
		@SuppressWarnings("unchecked")
		public <T extends WOComponent> T pageWithName(Class<T> componentClass, WOContext context) {
			    return (T)WOApplication.application().pageWithName(componentClass.getName(), context);
		}
		
		public boolean hasChildItems() {
			return childItems != null && !childItems.isEmpty();
		}
		
		public boolean hasHref() {
			return StringUtils.isNotBlank(href);
		}
	}
	
	
	/**
	 * Interface Delegate which gets called on different actions
	 * @author robin
	 *
	 */
	public static interface Delegate {
		boolean navigationBarItemIsSelected(BSNavigationBar navigationBar, Item item);
		WOActionResults navigationBarItemOnSelect(BSNavigationBar navigationBar, Item item);
	}
	
	
	private String _navbarElementID;
	
	/**
	 * Constructor
	 * @param context
	 */
	public BSNavigationBar(WOContext context) {
        super(context);
    }
	
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		_navbarElementID = null;
	}
	
	/**
	 * Returns "active" when the delegate says it's selected
	 * @return CSS class name
	 */
	public String cssClassLiItem() {
		Item item = (Item) valueForBinding("item");
		NSMutableArray<String> entries = new NSMutableArray<String>();
		
		if (delegate() != null && delegate().navigationBarItemIsSelected(this, item)) {
			entries.addObject("active");
		}
		
		if (item.hasChildItems()) {
			entries.addObject("dropdown");
		}
		
		return entries.isEmpty() ? null : entries.componentsJoinedByString(" ");
	}
	

	/**
	 * Select the current item inside the repetition
	 * @return
	 */
	public WOActionResults selectItem() {
		Item item = (Item) valueForBinding("item");
		setValueForBinding(item, "selection");
		if (delegate() != null) {
			delegate().navigationBarItemOnSelect(this, item);
		}
		return item.action(context());
	}
	

	/**
	 * Action for defaultItem.
	 * @return item.action or null
	 */
	public WOActionResults selectDefaultItem() {
		if (canGetValueForBinding("defaultItem")) {
			Item item = (Item) valueForBinding("defaultItem");
			if (item != null) {
				return item.action(context());
			}
		}
		
		return null;
	}
	
	
	/**
	 * Returns the navigation-bar's DOM element ID for this component.
	 * @return DOM element id
	 */
	public String navbarElementID() {
		if (ERXStringUtilities.isBlank(_navbarElementID)) {
			// We have to replace all "." chars by "-" otherwise it won't work
			// as a DOM selector at all.
			_navbarElementID = "navbar-" + StringUtils.replace(context().elementID(), ".", "-");
		}
		return _navbarElementID;
	}
	
	
	/**
	 * Helper Method. Returns the delegate which was set via binding or null.
	 * @return Delegate
	 */
	private Delegate delegate() {
		Delegate delegate = null;
		
		if (canGetValueForBinding("delegate")) {
			delegate = (Delegate) valueForBinding("delegate");
		}
		
		return delegate;
	}
	
	public static <T extends WOComponent> Item navigationItem(String label, Class<T> page){
		final Class<T> aPage = page;
		return new Item(label, page) {	 
			@Override
			public WOActionResults action(WOContext context) {
				return pageWithName(aPage, context);
			}
		};
	}
	
}