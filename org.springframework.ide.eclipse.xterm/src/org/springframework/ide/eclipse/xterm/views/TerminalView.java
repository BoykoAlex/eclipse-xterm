package org.springframework.ide.eclipse.xterm.views;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.springframework.ide.eclipse.xterm.XtermPlugin;

public class TerminalView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.springframework.ide.eclipse.xterm.views.TerminalView";

	@Inject IWorkbench workbench;
	
	private Action refreshAction;

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
									refresh();
									return Status.OK_STATUS;
								} finally {
									refreshJob = null;
								}
							}
					 };
					 refreshJob.schedule();
				}
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
		browser.setUrl(XtermPlugin.getDefault().xtermUrl(terminalId, null, null));
	}
	
	public void startTerminal(String terminalId, String cmd, String cwd) {
		this.terminalId = terminalId;
		browser.setUrl(XtermPlugin.getDefault().xtermUrl(terminalId, cmd, cwd));
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
	}

	private void makeActions() {
		refreshAction = new Action() {
			public void run() {
				refresh();
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refresh Terminal");
		refreshAction.setImageDescriptor(XtermPlugin.imageDescriptorFromPlugin(XtermPlugin.getDefault().getBundle().getSymbolicName(), "icons/refresh.png"));		
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
