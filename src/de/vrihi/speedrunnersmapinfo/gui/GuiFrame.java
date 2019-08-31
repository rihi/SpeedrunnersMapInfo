package de.vrihi.speedrunnersmapinfo.gui;

import de.vrihi.speedrunners.mapdecompiler.Converter;
import de.vrihi.speedrunners.mapdecompiler.data.Attribute;
import de.vrihi.speedrunners.mapdecompiler.data.Entity;
import de.vrihi.speedrunners.mapdecompiler.data.Layer;
import de.vrihi.speedrunners.mapdecompiler.data.SpeedrunnersMapData;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class GuiFrame
{
	private JFrame frame;

	private JMenuBar jMenuBar;
	private JMenu jMenuFile;
	private JMenuItem jMenuFileOpen;

	private JPanel contentPane;
	private JLabel lblMapFormat;
	private JLabel lblEntityCount;
	private JLabel lblLayerCount;
	private JPanel panelGeneralInfo;
	private JTextField txtMapFormt;
	private JTextField txtEntityCount;
	private JTextField txtLayerCount;
	private JPanel panelWorkshop;
	private JLabel lblTheme;
	private JTextField txtTheme;
	private JLabel lblAuthor;
	private JLabel lblMapName;
	private JLabel lblWorkshopId;
	private JButton btnOpenWorkshop;
	private JTextField txtAuthor;
	private JTextField txtMapName;
	private JTextField txtWorkshopId;
	private JScrollPane scrollEntities;
	private JList<String> listEntities;
	private JPanel panelEntityInformation;
	private JPanel panelEntitiyInformationContainer;
	private JLabel lblCoordinates;
	private JTextField txtCoordinates;
	private JLabel lblDimensions;
	private JTextField txtDimension;
	private JLabel lblEntitiyName;
	private JTextField txtEntityName;
	private JScrollPane scrollPaneEntitiyAttributes;
	private JTable tblEntitiyAttributes;
	private JScrollPane scrollLayers;
	private JList<String> listLayers;
	private JPanel panelLayerInformation;
	private JLabel lblLayerName;
	private JLabel lblLayerDimension;
	private JTextField txtLayerName;
	private JTextField txtLayerDimension;

	private JFileChooser openFileChooser;

	private SpeedrunnersMapData mapData = new SpeedrunnersMapData();

	public GuiFrame()
	{
		frame = new JFrame("GuiFrame");
		frame.setContentPane(contentPane);

		openFileChooser = new JFileChooser();
		openFileChooser.setFileFilter(new FileNameExtensionFilter("SpeedrunnersMap Files (*.sr|*.xnb)", "sr", "xnb"));

		jMenuBar = new JMenuBar();
		jMenuFile = new JMenu("File");
		jMenuFileOpen = new JMenuItem("Open");
		jMenuFileOpen.addActionListener(e -> {
			if (openFileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
				return;

			try
			{
				setInformationFromMap(Converter.read(openFileChooser.getSelectedFile().toPath()));
			} catch (Throwable ex) {
				JOptionPane.showMessageDialog(frame, "An error occurred while trying to load file. Not SpeedrunnersMapFile?", "Error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		});

		jMenuFile.add(jMenuFileOpen);
		jMenuBar.add(jMenuFile);
		frame.setJMenuBar(jMenuBar);

		btnOpenWorkshop.addActionListener(e -> {
			if (Desktop.isDesktopSupported())
			{
				try
				{
					Desktop.getDesktop().browse(new URI("https://steamcommunity.com/workshop/filedetails/?id=" + txtWorkshopId.getText()));
				} catch (IOException | URISyntaxException ex) {
					JOptionPane.showMessageDialog(frame, "An exception occurred: " + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(frame, "'Desktop' is not supported", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});


		listEntities.setModel(new AbstractListModel<String>() {
			@Override
			public int getSize()
			{
				return mapData.entities.length;
			}

			@Override
			public String getElementAt(int index)
			{
				return mapData.entities[index].elementName;
			}
		});
		listEntities.addListSelectionListener(e -> {
			Entity selectedEntity = getSelectedEntity();

			if (selectedEntity == null)
			{
				txtEntityName.setText("");
				txtCoordinates.setText("");
				txtDimension.setText("");
			} else {
				txtEntityName.setText(selectedEntity.elementName);
				txtCoordinates.setText(selectedEntity.xCoordinate + ", " + selectedEntity.yCoordinate);
				txtDimension.setText(selectedEntity.width + ", " + selectedEntity.height);
			}

			tblEntitiyAttributes.updateUI();
		});
		listLayers.setModel(new AbstractListModel<String>() {
			@Override
			public int getSize()
			{
				return mapData.layers.length;
			}

			@Override
			public String getElementAt(int index)
			{
				return mapData.layers[index].name;
			}
		});
		listLayers.addListSelectionListener(e -> {
			Layer selectedLayer = getSelectedLayer();

			if (selectedLayer == null)
			{
				txtLayerName.setText("");
				txtLayerDimension.setText("");
			} else {
				txtLayerName.setText(selectedLayer.name);
				txtLayerDimension.setText(selectedLayer.width + ", " + selectedLayer.height);
			}
		});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setMinimumSize(frame.getSize());
		frame.setVisible(true);
	}

	public void setInformationFromMap(SpeedrunnersMapData map)
	{
		mapData = map;

		txtMapFormt.setText(String.valueOf(map.mapFormat));
		txtEntityCount.setText(String.valueOf(map.entities.length));
		txtLayerCount.setText(String.valueOf(map.layers.length));
		txtTheme.setText(map.theme.toString());

		txtAuthor.setText(map.author != null ? map.author : "");
		txtMapName.setText(map.mapName != null ? map.mapName : "");
		txtWorkshopId.setText(map.workshopId != 0 ? map.getWorkshopId() : "");

		if (map.workshopId != 0)
			btnOpenWorkshop.setEnabled(true);
		else
			btnOpenWorkshop.setEnabled(false);

		Arrays.sort(mapData.entities, Comparator.comparing(entity -> entity.elementName));
		listEntities.updateUI();
		listLayers.updateUI();
		frame.pack();
	}

	private Entity getSelectedEntity()
	{
		int selectedEntitiyIndex = listEntities.getSelectedIndex();
		if (selectedEntitiyIndex == -1 || selectedEntitiyIndex >= mapData.entities.length)
			return null;
		else
			return mapData.entities[selectedEntitiyIndex];
	}

	private Layer getSelectedLayer()
	{
		int selectedLayerIndex = listLayers.getSelectedIndex();
		if (selectedLayerIndex == -1 || selectedLayerIndex >= mapData.layers.length)
			return null;
		else
			return mapData.layers[selectedLayerIndex];
	}

	private void createUIComponents()
	{
		tblEntitiyAttributes = new JTable(new AbstractTableModel() {
			@Override
			public int getRowCount()
			{
				Entity selectedEntity = getSelectedEntity();
				if (selectedEntity == null)
					return 0;
				else
					return selectedEntity.attributes.length;
			}

			@Override
			public int getColumnCount()
			{
				return 2;
			}

			@Override
			public String getColumnName(int columnIndex)
			{
				switch (columnIndex)
				{
					case 0: return "Key";
					case 1: return "Value";
					default: return "";
				}
			}

			@Override
			public Class<?> getColumnClass(int columnIndex)
			{
				return String.class;
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex)
			{
				return false;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex)
			{
				Entity selectedEntity = getSelectedEntity();

				if (rowIndex >= selectedEntity.attributes.length)
					return "null";

				Attribute attribute = selectedEntity.attributes[rowIndex];
				if (columnIndex == 0)
					return attribute.key;
				else
					return attribute.value;
			}
		});
	}

	public static GuiFrame createAndSetLookAndFeel()
	{
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return new GuiFrame();
	}
}
