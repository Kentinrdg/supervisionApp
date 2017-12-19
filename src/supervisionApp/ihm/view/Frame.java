package supervisionApp.ihm.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import supervisionApp.ihm.model.CPUChartModel;
import supervisionApp.ihm.model.FileServer;
import supervisionApp.ihm.model.IChartModelListener;
import supervisionApp.ihm.model.MyTableModel;

public class Frame extends JFrame {

	private static final long serialVersionUID = 3455953196718941645L;

	private CPUChartModel chartModel = null;
	private JTabbedPane tabPane = null;

	private int refreshingPeriod = 0;
	private int refreshingCPUPeriod = 0;

	private boolean enableMenuItem = false;

	private boolean showInKo = false;

	public Frame(boolean isConnectedMode) {

		UIManager.put("TabbedPane.selected", Color.white);
		UIManager.put("MenuBar.background", MyColor.whiteGrey);

		UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
		UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);

		// Menu bar
		JMenuBar menubar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		JMenu menuRefreshPeriod = new JMenu("Refreshing period");
		JMenu menuPreference = new JMenu("Preference");

		JMenuItem menuItemCPUPeriod = new JMenuItem("Change CPU refreshing period");
		JMenuItem menuItemStopRefreshingTablePeriod = new JMenuItem("Start refreshing table period");
		JMenuItem menuItemStartRefreshingTablePeriod = new JMenuItem("Stop refreshing table period");
		JMenuItem menuItemChangeRefreshingPeriod = new JMenuItem("Change process memory refreshing period");
		JMenuItem menuItemExit = new JMenuItem("Exit");

		JCheckBoxMenuItem menuChangeSizeKo = new JCheckBoxMenuItem("Change size on Ko");

		menuItemExit.setIcon(new ImageIcon("icons\\exit.png"));
		menuItemChangeRefreshingPeriod.setIcon(new ImageIcon("icons\\refresh.png"));
		menuItemCPUPeriod.setIcon(new ImageIcon("icons\\line-chart.png"));

		menuItemStartRefreshingTablePeriod.setIcon(new ImageIcon("icons\\play.png"));
		menuItemStopRefreshingTablePeriod.setIcon(new ImageIcon("icons\\stop.png"));

		menuRefreshPeriod.add(menuItemCPUPeriod);
		menuRefreshPeriod.add(menuItemChangeRefreshingPeriod);
		menuRefreshPeriod.addSeparator();
		menuRefreshPeriod.add(menuItemStartRefreshingTablePeriod);
		menuRefreshPeriod.add(menuItemStopRefreshingTablePeriod);

		// menuFile.addSeparator();
		menuFile.add(menuItemExit);

		menuPreference.add(menuChangeSizeKo);
		menuChangeSizeKo.setSelected(false);

		menubar.add(menuFile);
		menubar.add(menuRefreshPeriod);
		menubar.add(menuPreference);

		menuItemExit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		MyTableModel tableModel = new MyTableModel();

		menuItemChangeRefreshingPeriod.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String refreshingPeriod = JOptionPane.showInputDialog(Frame.this, "Choose your refreshing period (ms)");
				Frame.this.refreshingPeriod = Integer.valueOf(refreshingPeriod);
				tableModel.setRefreshingPeriod(Frame.this.refreshingPeriod);
			}
		});

		menuItemCPUPeriod.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String refreshingCPUPeriod = JOptionPane.showInputDialog(Frame.this,
						"Choose your CPU refreshing period (ms)");
				Frame.this.refreshingCPUPeriod = Integer.valueOf(refreshingCPUPeriod);
				if (chartModel != null) {
					chartModel.setRefreshingCPUPeriod(Frame.this.refreshingCPUPeriod);
				}
			}
		});

		menuChangeSizeKo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showInKo = menuChangeSizeKo.isSelected();
				tableModel.setShowOnKo(showInKo);
			}
		});

		setJMenuBar(menubar);
		setTitle("Process Manager");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Table tableExemple = new Table();
		tableExemple.setModel(tableModel);

		Map<String, String> mapNomTaille = tableModel.getMapNomTaille();

		JTabbePanelData tabPane = new JTabbePanelData();

		ChartPie chartPie = new ChartPie(mapNomTaille);
		JPanel createDemoPanel = chartPie.createChartPanel();
		chartPie.startMonitoring(tabPane, createDemoPanel);

		// Launch CPUChartPanel
		CPUChartPanel cpuChartPanel = new CPUChartPanel();
		chartModel = new CPUChartModel(new IChartModelListener() {
			@Override
			public void dataChanged(XYDataset dataset) {
				JFreeChart chart = cpuChartPanel.getChart();
				XYPlot plot = chart.getXYPlot();
				plot.setDataset(dataset);
			}
		});

		menuItemStartRefreshingTablePeriod.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.startMonitoring();
				menuItemStartRefreshingTablePeriod.setEnabled(enableMenuItem);
				enableMenuItem = false;
			}
		});

		menuItemStopRefreshingTablePeriod.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.stopMonitoring();
				menuItemStartRefreshingTablePeriod.setEnabled(!enableMenuItem);
				enableMenuItem = true;
			}
		});

		JVMProcessChart jvmProcessChart = new JVMProcessChart();
		JPanel panelJVMProcess = jvmProcessChart.createChartPanel();
		jvmProcessChart.startMonitoring(tabPane, panelJVMProcess);

		chartModel.startMonitoring();

		InformationPC informationPC = new InformationPC();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		// tabPane.setForeground(Color.white);
		tabPane.addMyTab("Process Memory", tableExemple);
		tabPane.setIconAt(0, (new ImageIcon("icons\\chip.png")));

		tabPane.addMyTab("Process Memory Graph", createDemoPanel);
		tabPane.setIconAt(1, (new ImageIcon("icons\\bar-chart.png")));
		tabPane.addMyTab("CPU", cpuChartPanel);
		tabPane.setIconAt(2, (new ImageIcon("icons\\line-chart.png")));
		tabPane.addMyTab("Informations System", informationPC);
		tabPane.setIconAt(3, (new ImageIcon("icons\\computer.png")));
		tabPane.addMyTab("JVM", panelJVMProcess);
		tabPane.setIconAt(4, (new ImageIcon("icons\\java.png")));

		setLayout(new BorderLayout());
		add(tabPane, BorderLayout.CENTER);

		// setContentPane(tabPane);
		setVisible(true);
		setSize(1000, 600);
		setIconImage(new ImageIcon("icons\\icon_frame.png").getImage());
		tableModel.startMonitoring();
	}

	public Frame(String modeConnected) {
		if (ChooseMode.MODE_CONNECTED.equals(modeConnected)) {
			FileServer fileServer = new FileServer();
			try {
				fileServer.startFileServer();
				new ShowClientPost();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		} else if (ChooseMode.MODE_LOCAL.equals(modeConnected)) {
			new Frame(false);
		}
	}

	public JTabbedPane getTabPane() {
		return tabPane;
	}

	public void setTabPane(JTabbedPane tabPane) {
		this.tabPane = tabPane;
	}

	public static void main(String[] args) {
		new Frame(false);
	}
}
