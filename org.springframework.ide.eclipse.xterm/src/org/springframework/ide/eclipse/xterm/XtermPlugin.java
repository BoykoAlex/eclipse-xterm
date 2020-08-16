package org.springframework.ide.eclipse.xterm;
import java.util.UUID;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.xterm.views.TerminalView;

public class XtermPlugin extends AbstractUIPlugin {
	
	private static XtermPlugin plugin;
	
	public static final String BG_COLOR = "org.springframework.ide.eclipse.xterm.background"; 
	public static final String FG_COLOR = "org.springframework.ide.eclipse.xterm.foreground"; 
	public static final String SELECTION_COLOR = "org.springframework.ide.eclipse.xterm.selection"; 
	public static final String CURSOR_COLOR = "org.springframework.ide.eclipse.xterm.cursor"; 
	public static final String CURSOR_ACCENT_COLOR = "org.springframework.ide.eclipse.xterm.cursorAccent";
	
	public static final String PREFS_DEFAULT_SHELL_CMD = "org.springframework.ide.eclipse.xterm.defaultShellCmd";

	@Override
	public void start(BundleContext bundle) throws Exception {
		plugin = this;
	}

	@Override
	public void stop(BundleContext bundle) throws Exception {
		plugin = null;
	}
	
	public static XtermPlugin getDefault() {
		return plugin;
	}
	
	public String getXtermServiceUrl() {
		return "http://localhost:8080/terminal/";
	}
	
	public static void log(String m, Throwable t) {
		getDefault().getLog().error(m, t);
	}
	
	public void openTerminalView(String cmd, String cwd) throws Exception {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		String terminalId = UUID.randomUUID().toString();
		TerminalView terminalView = (TerminalView) page.showView(TerminalView.ID, terminalId, IWorkbenchPage.VIEW_ACTIVATE);
		terminalView.startTerminal(terminalId, cmd, cwd);
	}

}
