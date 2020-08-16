package org.springframework.ide.eclipse.xterm.views;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.springframework.ide.eclipse.xterm.XtermPlugin;


public class TerminalView extends ViewPart {

	private static final String UTF8 = "UTF8";

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.springframework.ide.eclipse.xterm.views.TerminalView";

	@Inject IWorkbench workbench;
	
	private Action action1;
	private Action action2;

	private Browser browser;
	
	private String terminalId = "default";
	
	private UIJob refreshJob;
	
	private final IPropertyChangeListener PROPERTY_LISTENER = new IPropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			switch (event.getProperty()) {
			case XtermPlugin.BG_COLOR:
			case XtermPlugin.FG_COLOR:
			case XtermPlugin.SELECTION_COLOR:
			case XtermPlugin.CURSOR_COLOR:
			case XtermPlugin.CURSOR_ACCENT_COLOR:
				if (refreshJob == null) {
					 refreshJob = new UIJob("Refresh Terminal") {
							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) {
								try {
									browser.setUrl(getUrl(createParameters()));
									return Status.OK_STATUS;
								} catch (UnsupportedEncodingException e) {
									return new Status(IStatus.ERROR, XtermPlugin.getDefault().getBundle().getSymbolicName(), "Failed to refresh terminal", e);
								} finally {
									refreshJob = null;
								}
							}
					 };
					 refreshJob.schedule();
				}
				refresh();
				break;
			default:
			}
		}

	};

	@Override
	public void createPartControl(Composite parent) {
		PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(PROPERTY_LISTENER);
		browser = new Browser(parent, SWT.NONE);
		makeActions();
		contributeToActionBars();		
		refresh();
	}
	
	public void refresh() {
		try {
			browser.setUrl(getUrl(createParameters()));
		} catch (UnsupportedEncodingException e) {
			XtermPlugin.log("Failed to refresh Terminal", e);
		}
	}
	
	private String getUrl(Map<String, String> params) {
		StringBuilder url = new StringBuilder(XtermPlugin.getDefault().getXtermServiceUrl());
		url.append(terminalId);
		if (!params.isEmpty()) {
			url.append("?");
			List<String> paramStrings = new ArrayList<>(params.size());
			for (Map.Entry<String, String> e : params.entrySet()) {
				if (e.getValue() != null && !e.getValue().isEmpty()) {
					paramStrings.add(e.getKey() + "=" + e.getValue());
				}
			}
			url.append(String.join("&", paramStrings));
		}
		return url.toString();
	}
	
	public void startTerminal(String terminalId, String cmd, String cwd) throws Exception {
		
		this.terminalId = terminalId;
		
		Map<String, String> params = createParameters();
		
		if (cmd != null && !cmd.isEmpty()) {
			params.put("cmd", URLEncoder.encode(cmd, UTF8));
		}
		if (cwd != null && !cwd.isEmpty()) {
			params.put("cwd", URLEncoder.encode(cwd, UTF8));
		}
		
		browser.setUrl(getUrl(params));
		
	}
	
	private Map<String, String> createParameters() throws UnsupportedEncodingException {
		Map<String, String> params = new HashMap<>();
		ColorRegistry colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
		params.put("bg", URLEncoder.encode(rgbToUrlParameter(colorRegistry.get(XtermPlugin.BG_COLOR).getRGB()), UTF8));
		params.put("fg", URLEncoder.encode(rgbToUrlParameter(colorRegistry.get(XtermPlugin.FG_COLOR).getRGB()), UTF8));
		RGB selectionColor = colorRegistry.get(XtermPlugin.SELECTION_COLOR).getRGB();
		// add transparency to selection color
		params.put("selection", URLEncoder.encode(rgbaToUrlParameter(new RGBA(selectionColor.red, selectionColor.green, selectionColor.blue, 51)), UTF8));
		params.put("cursor", URLEncoder.encode(rgbToUrlParameter(colorRegistry.get(XtermPlugin.CURSOR_COLOR).getRGB()), UTF8));
		params.put("cursorAccent", URLEncoder.encode(rgbToUrlParameter(colorRegistry.get(XtermPlugin.CURSOR_ACCENT_COLOR).getRGB()), UTF8));
		params.put("cmd", URLEncoder.encode(XtermPlugin.getDefault().getPreferenceStore().getString(XtermPlugin.PREFS_DEFAULT_SHELL_CMD), UTF8));
		params.put("cwd", URLEncoder.encode(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString(), UTF8));
		return params;
	}
	
	
	
	private String rgbToUrlParameter(RGB rgb) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder("rgb(");
		sb.append(rgb.red);
		sb.append(",");
		sb.append(rgb.green);
		sb.append(",");
		sb.append(rgb.blue);
		sb.append(")");
		return sb.toString();
	}

	private String rgbaToUrlParameter(RGBA rgba) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder("rgba(");
		sb.append(rgba.rgb.red);
		sb.append(",");
		sb.append(rgba.rgb.green);
		sb.append(",");
		sb.append(rgba.rgb.blue);
		sb.append(",");
		sb.append(rgba.alpha / 255.0);
		sb.append(")");
		return sb.toString();
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(workbench.getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
			browser.getShell(),
			"Sample View",
			message);
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}

	@Override
	public void dispose() {
		PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(PROPERTY_LISTENER);
	}
	
	
}
