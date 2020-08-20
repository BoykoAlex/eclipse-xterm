package org.springframework.ide.eclipse.xterm;
import java.util.UUID;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.themes.ITheme;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.boot.core.SimpleUriBuilder;
import org.springframework.ide.eclipse.xterm.views.TerminalView;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class XtermPlugin extends AbstractUIPlugin {
	
	private static XtermPlugin plugin;
	
	public static final String BG_COLOR = "org.springframework.ide.eclipse.xterm.background"; 
	public static final String FG_COLOR = "org.springframework.ide.eclipse.xterm.foreground"; 
	public static final String SELECTION_COLOR = "org.springframework.ide.eclipse.xterm.selection"; 
	public static final String CURSOR_COLOR = "org.springframework.ide.eclipse.xterm.cursor"; 
	public static final String CURSOR_ACCENT_COLOR = "org.springframework.ide.eclipse.xterm.cursorAccent";
	public static final String FONT = "org.springframework.ide.eclipse.xterm.font";
	
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

	private String getXtermServiceUrl(String terminalId) {
		return "http://localhost:8080/terminal/" + terminalId;
	}
	
	public static void log(String m, Throwable t) {
		getDefault().getLog().error(m, t);
	}
	
	public void openTerminalView(String cmd, String cwd) {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			String terminalId = UUID.randomUUID().toString();
			TerminalView terminalView = (TerminalView) page.showView(TerminalView.ID, terminalId, IWorkbenchPage.VIEW_ACTIVATE);
			terminalView.startTerminal(terminalId, cmd, cwd);
		} catch (Exception e) {
			XtermPlugin.log(e);
		}
	}

	public static void log(Throwable e) {
		if (ExceptionUtil.isCancelation(e)) {
			//Don't log canceled operations, those aren't real errors.
			return;
		}
		try {
			XtermPlugin.getDefault().getLog().log(ExceptionUtil.status(e));
		} catch (NullPointerException npe) {
			//Can happen if errors are trying to be logged during Eclipse's shutdown
			e.printStackTrace();
		}
	}
	
	public String xtermUrl(String terminalId, String cmd, String cwd){
		SimpleUriBuilder urlBuilder = new SimpleUriBuilder(getXtermServiceUrl(terminalId));
		
		ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
		ColorRegistry colorRegistry = theme.getColorRegistry();
		Font font = theme.getFontRegistry().get(XtermPlugin.FONT);
		
		urlBuilder.addParameter("bg", rgbToUrlParameter(colorRegistry.get(XtermPlugin.BG_COLOR).getRGB()));
		urlBuilder.addParameter("fg", rgbToUrlParameter(colorRegistry.get(XtermPlugin.FG_COLOR).getRGB()));
		RGB selectionColor = colorRegistry.get(XtermPlugin.SELECTION_COLOR).getRGB();
		// add transparency to selection color
		urlBuilder.addParameter("selection", rgbaToUrlParameter(new RGBA(selectionColor.red, selectionColor.green, selectionColor.blue, 51)));
		urlBuilder.addParameter("cursor", rgbToUrlParameter(colorRegistry.get(XtermPlugin.CURSOR_COLOR).getRGB()));
		urlBuilder.addParameter("cursorAccent", rgbToUrlParameter(colorRegistry.get(XtermPlugin.CURSOR_ACCENT_COLOR).getRGB()));

		urlBuilder.addParameter("fontFamily", font.getFontData()[0].getName());
		urlBuilder.addParameter("fontSize", String.valueOf(font.getFontData()[0].getHeight()));
		
		if (cmd != null && !cmd.isEmpty()) {
			urlBuilder.addParameter("cmd", cmd);
		} else {
			urlBuilder.addParameter("cmd",
					XtermPlugin.getDefault().getPreferenceStore().getString(XtermPlugin.PREFS_DEFAULT_SHELL_CMD));
		}
		if (cwd != null && !cwd.isEmpty()) {
			urlBuilder.addParameter("cwd", cwd);
		} else {
			urlBuilder.addParameter("cwd", ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
		}
		
		return urlBuilder.toString();
			
	}
	
	private String rgbToUrlParameter(RGB rgb) {
		StringBuilder sb = new StringBuilder("rgb(");
		sb.append(rgb.red);
		sb.append(",");
		sb.append(rgb.green);
		sb.append(",");
		sb.append(rgb.blue);
		sb.append(")");
		return sb.toString();
	}

	private String rgbaToUrlParameter(RGBA rgba) {
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
	
}
