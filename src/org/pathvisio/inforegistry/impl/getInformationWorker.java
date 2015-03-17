package org.pathvisio.inforegistry.impl;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.bridgedb.Xref;
import org.pathvisio.inforegistry.IInfoProvider;

public class getInformationWorker extends SwingWorker<JComponent, Void> {

	private final IInfoProvider ipo;
	private final JPanel centerPanel;
	private final Xref xref;

	public getInformationWorker(final IInfoProvider ipo, final JPanel centerPanel, final Xref xref) {
		this.ipo = ipo;
		this.centerPanel = centerPanel;
		this.xref = xref;
	}

	@Override
	protected JComponent doInBackground() {
		centerPanel.add(new JLabel("Connecting to plugin..."));
		centerPanel.repaint();
		centerPanel.revalidate();
		return ipo.getInformation(xref);
	}

	protected void done() {
		try {
			JComponent jc = get();
			centerPanel.removeAll();
			centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
			centerPanel.add(jc);
			centerPanel.revalidate();
			centerPanel.repaint();
		} catch (InterruptedException e) {
			centerPanel.add(new JLabel("<html>Error: Could not connect to plugin.<br/>" + e.getMessage() + "</html>"));
			centerPanel.repaint();
			centerPanel.revalidate();
		} catch (ExecutionException e) {
			centerPanel.add(new JLabel("<html>Error: Could not connect to plugin.<br/>" + e.getMessage() + "</html>"));
			centerPanel.repaint();
			centerPanel.revalidate();
		} catch (CancellationException e) {
		}
	}
}
