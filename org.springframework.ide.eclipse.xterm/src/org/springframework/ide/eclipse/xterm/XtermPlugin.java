package org.springframework.ide.eclipse.xterm;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

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

}
