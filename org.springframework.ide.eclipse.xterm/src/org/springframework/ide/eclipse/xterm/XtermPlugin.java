package org.springframework.ide.eclipse.xterm;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.boot.core.SimpleUriBuilder;
import org.springframework.ide.eclipse.xterm.views.TerminalView;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class XtermPlugin extends AbstractUIPlugin {
	
	private static XtermPlugin plugin;

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
	
	public void openTerminal(String cmd, String workingDir) {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (page!=null) {
				String sessionId = UUID.randomUUID().toString();
				TerminalView view = (TerminalView) page.showView(TerminalView.ID, sessionId, IWorkbenchPage.VIEW_ACTIVATE);
				SimpleUriBuilder url = new SimpleUriBuilder("http://localhost:8080/terminal/"+sessionId);
				if (cmd!=null) {
					url.addParameter("cmd", cmd);
				}
				if (workingDir!=null) {
					url.addParameter("cwd", workingDir);
				}
				view.url.setValue(url.toString());
			}
		} catch (Exception e) {
			Log.log(e);
		}
	}

}
