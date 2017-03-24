package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.jfree.chart.ChartPanel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import controller.Starter;
import model.database.DBConnection;
import model.datatypes.DistanceCalculator;
import model.datatypes.Hose;
import model.datatypes.HoseLine;
import model.datatypes.HosePoint;
import model.datatypes.Hydrant;
import model.datatypes.IncidentScene;
import model.datatypes.Pump;
import model.datatypes.Settings;
import model.datatypes.Way;
import model.datatypes.WayNode;
import model.datatypes.WaySegment;
import view.chart.Chart;

/**
* Demonstrates the usage of {@link JMapViewer}
*
* @author Jan Peter Stotz
*
*/
public class Osmmap_webservice extends JFrame implements JMapViewerEventListener  {

	private static final long serialVersionUID = 1L;
	
	private JMapViewer map;
	
	private JPanel panel;
	
	private long starttime = 0;
	
	private List<Hydrant> allHydrants;
	
	private IncidentScene scene;
	private long idNearestWayOfIncident = 0;
	
	private List<Way> ways;
	private List<HoseLine> hoseLines;
	
//	private Integer[] proposedHoselines = new Integer[]{0,1,2};
	private Integer[] proposedHoselines = new Integer[]{0,1,2};
	private HoseLine selectedHoseLine;
	private Way selectedWay;
	
	private List<WaySegment> blockedWaySegments;
	private List<HosePoint> blockedHydrants;
	private List<HoseLine> nonBlockedHoseLines;
	
	private JComboBox<String> proposedLinesCombo;
	private JLabel lineLenghtLabel;
	private JLabel hoseNumberLabel;
	private JLabel pumpsLabel;
	private JLabel typeLabel;
	private JLabel diameterLabel;
	private JLabel positionLabel;
	private ChartPanel chartPanel;
	private Chart elevationChart;
	
	
	private boolean changed = false;
	
	private String TYPE;

	/**
	 * Constructs the {@code OsmMap}.
	 * @param type - database type (local or biggis server)
	 */
	public Osmmap_webservice(String type) {
		super("Fire Fighter Tool");
		
		TYPE = type;
		
		setAllNew();
	     
 	}
	
	protected void setAllNew() {
		map = new JMapViewer();
		
        setSize(400,400);
        
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(new LineBorder(Color.black));
        add(panel, BorderLayout.EAST);
        
        JLabel dragDrop = new JLabel();
        dragDrop.setFont(dragDrop.getFont().deriveFont(Font.BOLD));
        dragDrop.setText("<html><body>  Drag 'n' Drop Vehicle <br /> to Incident Scene:<br /></body></html>");
        dragDrop.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(dragDrop);
        
        ImageIcon icon = new ImageIcon("lib/truck_med.png");
        JLabel thumb = new JLabel();
        thumb.setIcon(icon);
        MouseListener listener = new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
            	Toolkit toolkit = Toolkit.getDefaultToolkit();
            	Image image = toolkit.getImage("lib/truck_tiny.png");
            	Cursor c = toolkit.createCustomCursor(image, new Point(15,15), "Truck");
            	setCursor(c);
            }
            
            public void mouseReleased(MouseEvent e) {
            	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            	if (scene != null) {
            		int eingabe = JOptionPane.showConfirmDialog(null,
                            "Set a new Incident Scene?",
                            "New Incident Scene",
                            JOptionPane.YES_NO_OPTION);
            		
            		System.out.println("YES:"+JOptionPane.YES_OPTION+ "  NO:"+JOptionPane.NO_OPTION +"  CANCEL:"+JOptionPane.CANCEL_OPTION +"  CLOSE:"+JOptionPane.CLOSED_OPTION);
            		if (eingabe == JOptionPane.NO_OPTION) return;
            		else {
            			MapMarker oldScene = null;
            			for (MapMarker m: map().getMapMarkerList()) {
            				if (m.getClass() == IncidentScene.class) {
            					oldScene = m;
            				}
            			}
            			// remove the truck
            			map().removeMapMarker(oldScene);
            			scene = null;
            			
//            			resetAll();
            		}
            	}
            	setIncidentScene(e.getLocationOnScreen().getLocation());
            	SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						changed = true;
						calculate();
						changed = false;
					}
				});
            }

			
          };
        thumb.addMouseListener(listener);
        thumb.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(thumb);
        
        JLabel flowRateLabel = new JLabel();
        flowRateLabel.setText("Flowrate in l/min:");
        flowRateLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(flowRateLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        
        final JTextField flowRateTextField = new JTextField();
        flowRateTextField.setText(""+Settings.getInstance().getFlowRate());
        flowRateTextField.setMaximumSize(new Dimension(500, 30));
        flowRateTextField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
			        Settings.getInstance().setFlowrate(new Integer(flowRateTextField.getText()));
				} catch (NumberFormatException ex) {
					flowRateTextField.setText("Please insert an integer.");
				}
				changed = true;
				calculate();
				changed = false;
			}
		});
        panel.add(flowRateTextField);
        
        JLabel numberLinesLabel = new JLabel();
        numberLinesLabel.setText("Number of parallel Lines:");
        numberLinesLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(numberLinesLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        
        Integer[] possibleNumbers = new Integer[]{1,2};
        final JComboBox<Integer> numberLinesCombo = new JComboBox<Integer>(possibleNumbers);
        numberLinesCombo.setSelectedItem(Settings.getInstance().getNumberOfLines());
        numberLinesCombo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Settings.getInstance().setNumberOflInes((Integer)numberLinesCombo.getSelectedItem());
				changed = true;
				calculate();
				changed = false;
			}
		});
        numberLinesCombo.setMaximumSize(new Dimension(500, 30));
        panel.add(numberLinesCombo);
        
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        
        JLabel proposedLineLabel = new JLabel();
        proposedLineLabel.setText("Select Hoseline:");
        proposedLineLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(proposedLineLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        
        String[] proposal = new String[]{"No Proposal yet."};
        proposedLinesCombo = new JComboBox<>(proposal);
        proposedLinesCombo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = proposedLinesCombo.getSelectedIndex();
				if(index == -1) return;
				setAllInvisible();
				displayHoseLine(index);
			}
		});
        proposedLinesCombo.setMaximumSize(new Dimension(500, 30));
        panel.add(proposedLinesCombo);
        
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        
        elevationChart = new Chart();
        chartPanel = elevationChart.createChart(null);
        chartPanel.setPreferredSize(new Dimension(200, 150));
        chartPanel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JFrame chartFrame = new JFrame("Elevation Profile");
					chartFrame.pack();
					chartFrame.add(chartPanel);
					chartFrame.setVisible(true);
					chartFrame.setSize(600, 500);
				}
			}
		});
        chartPanel.setMaximumSize(new Dimension(500, 150));
        panel.add(chartPanel);
        
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        
        lineLenghtLabel = new JLabel();
        lineLenghtLabel.setText("Total Length of Hoseline:");
        lineLenghtLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lineLenghtLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        
        hoseNumberLabel = new JLabel();
        hoseNumberLabel.setText("Total Number of Hoses:");
        hoseNumberLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(hoseNumberLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        
        pumpsLabel = new JLabel();
        pumpsLabel.setText("Total Number of Pumps:");
        pumpsLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(pumpsLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0,10)));
        
        typeLabel = new JLabel();
        typeLabel.setAlignmentX(CENTER_ALIGNMENT);
        typeLabel.setVisible(false);
        panel.add(typeLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        
        diameterLabel = new JLabel();
        diameterLabel.setText("Hydrant Diameter:");
        diameterLabel.setAlignmentX(CENTER_ALIGNMENT);
        diameterLabel.setVisible(false);
        panel.add(diameterLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        
        positionLabel = new JLabel();
        positionLabel.setText("Hydrant Position:");
        positionLabel.setAlignmentX(CENTER_ALIGNMENT);
        positionLabel.setVisible(false);
        panel.add(positionLabel);
        
        
        // offline tiles
//        URI path = new File(Settings.getInstance().getTilesPath()).toURI();
//        TileSource tileSource = new OfflineOsmTileSource(path.toString(),11,19);
        
        //online tiles
        TileSource tileSource = new OsmTileSource.CycleMap();
//        TileSource tileSource = new OsmTileSource.Mapnik();
//		map().setTileSource(tileSource);
		map.setTileSource(tileSource);
		
		
		TileLoader tileLoader = new OsmTileLoader(map);
		map.setTileLoader(tileLoader);
		
		// set to liggeringen
	    map.setDisplayPosition(new Coordinate(47.76035, 8.97), 13);
        
        map.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				mouseWasClicked(e);
			}
		});
        
        add(map, BorderLayout.CENTER);
        
        
        allHydrants = new ArrayList<Hydrant>();
        displayAllHydrants();
        
//      graphFile = new File("/Users/michaelhundt/Documents/Meine/Studium/MASTER/HIWI/Daten/osm2po-5.1.0/de/de_2po.gph");
//		graph = new Graph(graphFile);
	}
	
	private void resetAll() {
		selectedWay = null;
		selectedHoseLine = null;
//		proposedHoselines = new Integer[]{0,1,2};
		proposedHoselines = new Integer[]{0,1};
		idNearestWayOfIncident = 0;
		hoseLines = null;
		map.removeAllMapMarkers();
		map.removeAllMapPolygons();
		
		allHydrants = new ArrayList<Hydrant>();
        displayAllHydrants();
		
	}
	

	private void displayAllHydrants() {
		try {
			PreparedStatement st = null;
			// 20 km Buffer around point of Liggeringen  
			//TODO !!! Interactive Buffer !!!
			if (TYPE.equals(Starter.Type.BIGGIS.name())) {
				Connection c = DBConnection.getConnection(TYPE, true);
				st = c.prepareStatement("Select h.id,  st_astext(h.geom), h.height"
					+ " From (Select * from de_hydrants ) as h, "
					+ "(SELECT ST_Buffer(ST_SetSRID(ST_MakePoint(8.97, 47.76035), 4326)::geography, 20000)::geometry as geom) as buffer "
					+ "Where ST_Contains(buffer.geom, h.geom);");
				
//				st = c.prepareStatement("Select h.id,  st_astext(h.geom), h.height"
//						+ " From de_hydrants as h "
//						);
			}
			else {
				System.out.println("Hydrants Statement --> NULL POINTER \t TYPE:"+TYPE);
				return;
			}
			
			ResultSet rs = st.executeQuery();
			Hydrant h = null;
			while(rs.next()) {
				h = new Hydrant(getCoordinateFromString(rs.getString(2)), rs.getDouble(3));
				
				allHydrants.add(h);
				map().addMapMarker(h);
			}
			rs.close();
			st.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception a) {
			a.printStackTrace();
		}
	}

	protected void calculate() {
		
		setAllInvisible();
		
		starttime = System.currentTimeMillis();
		if (scene == null) {
			return;
		}
		
		if (idNearestWayOfIncident == 0) {
			idNearestWayOfIncident = getNearestWayPointID(scene);
		}
		if (ways == null) {
			ways = getPathToHydrants(idNearestWayOfIncident);
		}
		
		System.out.println("Getting all paths took: "+(System.currentTimeMillis()- starttime));
		starttime = System.currentTimeMillis();
		
		if (hoseLines == null || changed) {
			// pumpen und strecke berechnen
			hoseLines = new ArrayList<HoseLine>();
			for (int i = 0; i < ways.size();i++) {
				hoseLines.add(calculateHoseLine(ways.get(i)));
				hoseLines.get(hoseLines.size()-1).setWay(i);
			}
//			System.out.println(hoseLines.toString());
			hoseLines.sort(null);
//			System.out.println(hoseLines.toString());
		}
		
		System.out.println("Calc all paths took: "+(System.currentTimeMillis()- starttime));
		starttime = System.currentTimeMillis();
		
		HosePoint hy = hoseLines.get(0).getHoses().get(0).getStart();
		
		double angle1 = DistanceCalculator.courseAngle(scene.getLat(), scene.getLon(), hy.getLat(), hy.getLon());
		double angle2 = angle1;
		
		int numberofProposedHoselines = 0;
		
		nonBlockedHoseLines = new ArrayList<HoseLine>(hoseLines);
		
		if (blockedWaySegments == null && blockedHydrants == null) {
			for (int i = 0; i < nonBlockedHoseLines.size() && numberofProposedHoselines < 3;i++) {
				hy = nonBlockedHoseLines.get(i).getHoses().get(0).getStart();
				double angle = DistanceCalculator.courseAngle(scene.getLat(), scene.getLon(), hy.getLat(), hy.getLon());
				double difference1 = Math.min((360+angle-angle1) %360, (360+angle1-angle) %360);
				double difference2 = Math.min((360+angle-angle2) %360, (360+angle2-angle) %360);
				if (difference1 > 45 && difference2 %360 > 45|| numberofProposedHoselines== 0) {
					proposedHoselines[numberofProposedHoselines] = i;
					numberofProposedHoselines++;
					angle2 = angle;
				}
			}
		}
		else {
			for (int hl = 0; hl < hoseLines.size(); hl++) {
				HoseLine l = hoseLines.get(hl);
				boolean use = true;
				if (blockedHydrants != null) {
					for (HosePoint h : blockedHydrants) {
						if (l.getHoses().get(0).getStart() == h) {
							use = false;
						}
					}
				}
				if (blockedWaySegments != null && use) {
					for (WaySegment blockSeg : blockedWaySegments) {
						WayNode start = blockSeg.getStart();
						WayNode end = blockSeg.getEnd();
						for (WaySegment seg : ways.get(l.getWay()).getWaySegments()) {
							WayNode start1 = seg.getStart();
							WayNode end1 = seg.getEnd();
							// falls es geblocked ist
							if (start.getID() == start1.getID() && end.getID() == end1.getID()) {
								use = false;
							}
							if (start.getID() == end1.getID() && end.getID() == start1.getID()) {
								use = false;
							}
						}
					}
				}
				if(!use) {
					nonBlockedHoseLines.remove(l);
				}
				else {
					hy = l.getHoses().get(0).getStart();
					double angle = DistanceCalculator.courseAngle(scene.getLat(), scene.getLon(), hy.getLat(), hy.getLon());
					double difference1 = Math.min((360+angle-angle1) %360, (360+angle1-angle) %360);
					double difference2 = Math.min((360+angle-angle2) %360, (360+angle2-angle) %360);
					if (difference1 > 45 && difference2 %360 > 45 || numberofProposedHoselines== 0) {
						if (numberofProposedHoselines >= proposedHoselines.length)
							break;
						proposedHoselines[numberofProposedHoselines] = hl - (hoseLines.size() - nonBlockedHoseLines.size());
						numberofProposedHoselines++;
						angle2 = angle;
					}
				}
			}
		}


		int selIndex = proposedLinesCombo.getSelectedIndex();
		proposedLinesCombo.removeAllItems();
		for (int i = 0; i < proposedHoselines.length; i++) {
			proposedLinesCombo.addItem(nonBlockedHoseLines.get(proposedHoselines[i]).toString());
		}
		proposedLinesCombo.setSelectedIndex(selIndex);
		
		displayHoseLine(proposedLinesCombo.getSelectedIndex());
				
		
	}

	private void setAllInvisible() {
		for(MapPolygon m : map().getMapPolygonList()) {
			if (m.getClass() == Hose.class) {
				((Hose) m).setVisibility(false);
			}
			else if (m.getClass() == WaySegment.class) {
				((WaySegment) m).setVisibility(false);
			}
		}
		
		for(MapMarker m: map().getMapMarkerList()) {
			if(m.getClass() == IncidentScene.class) {
				continue;
			}
			else if (m.getClass() == Pump.class) {
				((Pump) m).setVisible(false);
			}
			else if (m.getClass() == Hydrant.class) {
				((Hydrant) m).setVisible(false);
			}
		}
	}

	private void displayHoseLine(int i) {
		
		if (blockedWaySegments != null) {
			for(WaySegment seg : blockedWaySegments) {
				seg.setVisibility(true);
				seg.setVisible(true);
				map().addMapPolygon(seg);
			}
		}
		
		for (Hose h : nonBlockedHoseLines.get(proposedHoselines[i]).getHoses()) {
			h.setVisibility(true);
			h.setVisible(true);
			map().addMapPolygon(h);
		}
		
		for (Pump p : nonBlockedHoseLines.get(proposedHoselines[i]).getPumps()) {
			p.setVisible(true);
			map().addMapMarker(p);
		}
		
		selectedWay = ways.get(nonBlockedHoseLines.get(proposedHoselines[i]).getWay());
		selectedHoseLine = nonBlockedHoseLines.get(proposedHoselines[i]);
		
		HosePoint firstHosePoint = nonBlockedHoseLines.get(proposedHoselines[i]).getHoses().get(0).getStart();
		
		Hydrant hydrant = findNearestHydrant(firstHosePoint.getCoordintes());
		hydrant.setBlackWhite(false);
		
		map().addMapMarker(hydrant);
		
		if (hydrant.getType() != null) {
	        typeLabel.setText("Hydrant Type: "+hydrant.getType().toUpperCase());
			typeLabel.setVisible(true);
		}
		else {
			typeLabel.setVisible(false);
		}
		if (hydrant.getDiameter() != null) {
	        diameterLabel.setText("Hydrant Diameter: "+hydrant.getDiameter());
	        diameterLabel.setVisible(true);
		}
		else {
			diameterLabel.setVisible(false);
		}
		if (hydrant.getPosition() != null) {
	        positionLabel.setText("Hydrant Position: "+hydrant.getPosition().toUpperCase());
	        positionLabel.setVisible(true);
		}
		else {
			positionLabel.setVisible(false);
		}
		
		
		double length = nonBlockedHoseLines.get(proposedHoselines[i]).getLength();
		
		lineLenghtLabel.setText("Total Length of Hoseline: "+(int)length+"m");
		hoseNumberLabel.setText("Total Number of Hoses: "+(int)Math.ceil(length/20));
		pumpsLabel.setText("Total Number of Pumps: "+nonBlockedHoseLines.get(proposedHoselines[i]).getPumps().size());
		
		panel.remove(chartPanel);
		chartPanel = elevationChart.createChart(nonBlockedHoseLines.get(proposedHoselines[i]));
		chartPanel.setPreferredSize(new Dimension(200, 150));
		chartPanel.setMaximumSize(new Dimension(500,150));
		chartPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mousePressed(MouseEvent e) {
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JFrame chartFrame = new JFrame("Elevation Profile");
					chartFrame.pack();
					chartFrame.add(chartPanel);
					chartFrame.setVisible(true);
					chartFrame.setSize(600, 500);
				}
			}
		});
		panel.add(chartPanel, 13);
		panel.validate();
	}

	private Hydrant findNearestHydrant(Coordinate coordintes) {
		try {
			PreparedStatement st = null;
			if (TYPE.equals(Starter.Type.BIGGIS.name())) {
//				st = DBConnection.getConnection(TYPE, false).prepareStatement(""
//					+ "SELECT h.hydrant_id, st_astext(n.way), "
//					+ "st_distance_sphere(n.way, st_geomfromtext('POINT("+coordintes.getLon()+" "+coordintes.getLat()+")')) as dist, "
//					+ "e.height, n.tags "
//					+ "FROM de_hydrantstowaypoints as h, de_point n, de_hydrants e "
//					+ "WHERE n.osm_id = h.hydrant_id and n.osm_id = e.id "
//					+ "ORDER BY dist asc "
//					+ "LIMIT 1;");
				
				// --> much FASTER!!
				st = DBConnection.getConnection(TYPE, false).prepareStatement(""
						+ "SELECT v.id, st_astext(v.geom), "
						+ "st_distance_sphere(v.geom, st_geomfromtext('POINT("+coordintes.getLon()+" "+coordintes.getLat()+")')) as dist, v.height, v.tags "
						+ "FROM de_hydrants as v "
						+ "ORDER BY v.geom <-> st_setsrid(st_geomfromtext('POINT("+coordintes.getLon()+" "+coordintes.getLat()+")'), 4326) "
						+ "LIMIT 1;");
				
			}
			else {
				System.out.println("Find nearest Hydrant Statement --> NULL POINTER \t TYPE:"+TYPE);
				return null;
			}
			
			ResultSet rs = st.executeQuery();
			Coordinate c = null;
			long id;
			double elevation = 0;
			Hydrant h = null;
			while(rs.next()) {
				id = rs.getLong(1);
				c = getCoordinateFromString(rs.getString(2));
				elevation = rs.getDouble(4);
				h = new Hydrant(c, elevation);
				h = setHydrantInfos(h, rs.getString(5));
			}
			st.close();
			rs.close();
			
			return h;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Hydrant setHydrantInfos(Hydrant h, String string) {
		String[] infos = string.split(", ");
		for (String s : infos) {
			int index = 0;
			String setString = "";
			if (s.contains("type")) {
				index = s.indexOf("type") + "type".length()+4;
				setString = s.substring(index,s.length()-1);
				h.setType(setString);
			}
			else if (s.contains("diameter")) {
				index = s.indexOf("diameter") + "diameter".length()+4;
				setString = s.substring(index,s.length()-1);
				h.setDiameter(setString);
			}
			else if (s.contains("position")) {
				index = s.indexOf("position") + "position".length()+4;
				setString = s.substring(index,s.length()-1);
				h.setPosition(setString);
			}
		}
		return h;
			
}

	protected void setIncidentScene(Point location) {
		if (scene != null) {
			return;
		}
		int x = location.x;
		int y = location.y - 20;
		scene = new IncidentScene(map().getPosition(x, y));
		map().addMapMarker(scene);
	}

	private HoseLine calculateHoseLine(Way way) {
		double pressureLoss100m = calculatePressureLoss(); // verlust pro 100m schlauchleitung
		
 		List<WaySegment> waySegments = way.getWaySegments();
		List<Pump> pumps = new ArrayList<Pump>();
		List<Hose> hoses = new ArrayList<Hose>();
		
		int numberOfHosesPump = 0;
		
		//add incidentscene
//		WaySegment toIncidentscene = new WaySegment(waySegments.get(0).getEnd(), new WayNode(2222222, scene.getCoordinate(), waySegments.get(0).getEnd().getElevation()));
		WaySegment toIncidentscene = new WaySegment(waySegments.get(0).getEnd(), new WayNode(-1, scene.getCoordinate(), waySegments.get(0).getEnd().getElevation()));

		waySegments.add(0, toIncidentscene);
		
		double totalLength = 0;
		
		for(int i = waySegments.size() -1; i >= 0; i--) {
			WayNode thisWayNode = waySegments.get(i).getStart();
			WayNode nextWayNode = waySegments.get(i).getEnd();
			
			double startPressure = 2;
			// falls es nicht das erste wegsegment ist
			// dann ist der anfangs druck = dem enddruck des letzen schlauches
			if (i != waySegments.size() -1 ) {
				startPressure = hoses.get(hoses.size()-1).getEnd().getPressure();
			}
			
			HosePoint start = new HosePoint(thisWayNode.getCoordinates(), thisWayNode.getElevation(), startPressure);
			
			double lengthOfWay = coordinateDistanceInMeters(thisWayNode.getCoordinates(), nextWayNode.getCoordinates());
			double completeWay = coordinateDistanceInMeters(thisWayNode.getCoordinates(), nextWayNode.getCoordinates());;
			
			
			// solange der restweg laenger als ein schlauch ist
			while (lengthOfWay > 20) {
				double newLon = (nextWayNode.getCoordinates().getLon()-start.getLon())*(20d/completeWay)+start.getLon();
				double newLat = (nextWayNode.getCoordinates().getLat()-start.getLat())*(20d/completeWay)+start.getLat();
				double newEle = (nextWayNode.getElevation()-start.getElevation())*(20d/completeWay)+start.getElevation();
				double elevationDiff = newEle-start.getElevation();
				double pressureLoss = pressureLoss100m*(20d/100) + (elevationDiff*0.1);
				double newPress = start.getPressure()-pressureLoss;
				if(newPress < 1.5) {
					Pump pump = new Pump(start.getCoordintes());
					pump.setPosition(totalLength);
					pump.setElevation(start.getElevation());
					pump.setHosePosition((int)totalLength/20 - numberOfHosesPump); 
					numberOfHosesPump = pump.getHosePosition();
					pumps.add(pump);
					newPress = 8.0;
				}
				HosePoint endPoint = new HosePoint(c(newLat, newLon), newEle, newPress);
				start.setPosition(totalLength);
				totalLength += 20;
				endPoint.setPosition(totalLength);
				Hose hose = new Hose(start, endPoint);
				hoses.add(hose);
				start = endPoint;
				lengthOfWay -= 20;
			}
			// weniger als eine schlauchlaenge distance
			double elevationDiff = nextWayNode.getElevation()-start.getElevation();
			double pressureLoss = pressureLoss100m*(lengthOfWay/100d) + (elevationDiff*0.1);
			double newPress = start.getPressure()-pressureLoss;
			if(newPress < 1.5) {
				Pump pump = new Pump(start.getCoordintes());
				pump.setPosition(totalLength);
				pump.setElevation(start.getElevation());
				pump.setHosePosition((int)totalLength/20 - numberOfHosesPump); 
				numberOfHosesPump = pump.getHosePosition();
				pumps.add(pump);
				newPress = 8.0;
			}
			HosePoint endPoint = new HosePoint(nextWayNode.getCoordinates(), nextWayNode.getElevation(), newPress);
			start.setPosition(totalLength);
			totalLength += lengthOfWay;
			endPoint.setPosition(totalLength);
			Hose hose = new Hose(start, endPoint);
			if (hose.getLength() != 0) {
				hoses.add(hose);
			}
			
		}
		
		return new HoseLine(hoses, pumps);
	}
	
	
	private long getNearestVertex(double lon, double lat) {
		long id = -1;
		String url = "http://localhost:8888/Osm2poService/?cmd=fv&lat="+lat+"&lon="+lon+"&format=geojson";
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");
			//add request header
//			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
//			System.out.println("\nSending 'GET' request to URL : " + url);
//			System.out.println("Response Code : " + responseCode);
			
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			//print result
//			System.out.println(response.toString());
			JSONObject jobj = new JSONObject(response.toString());
			JSONObject res = jobj.getJSONObject("properties");
			id = res.getInt("id");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println("Point("+lon+" "+lat+")  --> VertexID: "+id);
		
		return id;
	}
	
	
	private String getRoute(long source, long target) {
		String url = "http://localhost:8888/Osm2poService/?cmd=fr&source="+source+"&target="+target+"&findShortestPath=true&format=geojson";
		
		String route = "LINESTRING(";
		// optional default is GET
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			//add request header
			
			int responseCode = con.getResponseCode();
//			System.out.println("\nSending 'GET' request to URL : " + url);
//			System.out.println("Response Code : " + responseCode);
			
			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			if (response.toString().startsWith("#"))
				return "";
			JSONObject jobj = new JSONObject(response.toString());
			JSONArray farr = jobj.getJSONArray("features");
			
			for (int i = 0; i < farr.length(); i++) {
				JSONObject geometry = farr.getJSONObject(i).getJSONObject("geometry");
				JSONArray linestring = geometry.getJSONArray("coordinates");
				for (int j = 0; j < linestring.length(); j++) {
					JSONArray points = linestring.getJSONArray(j);
					double lon = (double)points.get(0);
					double lat = (double)points.get(1);
					route += lon+" "+lat+",";
				}
				
			}
			route = route.substring(0, route.length()-1)+")";
			
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return route;
	}
	

	// get paths to nearest 100 hydrants
	private List<Way> getPathToHydrants(long start) {
 		List<Way> paths = new ArrayList<Way>();
 		try {
			System.out.println("Calculate Routes");
			
			PreparedStatement height_getter = DBConnection.getConnection(TYPE, true).prepareStatement(""
					+ "Select ST_Value(rast, ST_SetSRID(st_geomfromtext( ? ) ,4326)) "
					+ "From elevation_srtm90_v4 "
					+ "Where ST_Intersects(rast, ST_SetSRID(st_geomfromtext( ?),4326),4326)"
			);
			
			String startPoint = "POINT("+scene.getLon()+" "+scene.getLat()+")";
			// Get 100 nearest hydrants
			PreparedStatement st = DBConnection.getConnection(TYPE, false).prepareStatement(""
					+ "Select v.id, st_astext(v.geom), v.height "
					+ "FROM de_hydrants as v "
					+ "ORDER BY v.geom <-> ST_SetSRID(st_geomfromtext('"+startPoint+"'),4326) "
					+ "LIMIT 20"
			);
			
			ResultSet rs = st.executeQuery();
			
			Way way = new Way();
			List<WayNode> waynodes = new ArrayList<WayNode>();
			
			int way_counter = 1;
			long node_counter = 0;
			while(rs.next()) {
//				DefaultRouter router = new DefaultRouter();
				long hy_id =  rs.getLong(1);
				String hydro = rs.getString(2);
				Coordinate coordi = getCoordinateFromString(hydro);
				// Get closest Vertex
				long targetId = getNearestVertex(coordi.getLon(), coordi.getLat());
				
//				System.out.println(counter++ +". Hydrant >> ID: "+hy_id+" \n"
//						+ "Nearest Node >> ID: "+targetId +"\n"
//						+ "Source Node >> ID: "+start);
				
				String route = getRoute(start, targetId);
				if (route.isEmpty())
					continue;
				
				route = route.replace("LINESTRING(", "");
				route = route.replace(")", "");
				String point_concat  = "";
				String[] points = route.split(",");
				for (String point : points){
					String p = "POINT("+point+")";
					point_concat += p+",";
				}
				System.out.println(way_counter+": "+route);
				point_concat = point_concat.substring(0, point_concat.length()-1);
				
				boolean first_node = false;
				WayNode first = null;
				boolean canceled_start = false;
				
//				System.out.println(point_concat);
				points = point_concat.split(",");
				
				double height = getHeightFromGeom(points[0], height_getter);
				first = new WayNode(node_counter++, getCoordinateFromString(points[0]), height);
				waynodes.add(first);
				
				// remove first?
				for (int i = 1; i< points.length; i++) {
//					double height = getHeightFromGeom(points[i], height_getter);
//					first = new WayNode(c, getCoordinateFromString(points[i]), height);
//					waynodes.add(first);
					height = getHeightFromGeom(points[i], height_getter);
					WayNode second = new WayNode(node_counter, getCoordinateFromString(points[i]), height);
					waynodes.add(second);
					WaySegment seg = new WaySegment(first, second);
//					System.out.println("seq:"+c+"  "+points[i-1]+" - "+points[i]+"  h:"+height);
					way.addWaySegments(seg);
					first = second;
					node_counter++;
				}
				
				way_counter++;
				paths.add(way);
				way = new Way();
				waynodes =  new ArrayList<WayNode>();
				
			}
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
 		
 		return paths;
	}
	
	
	public double getHeightFromGeom(String geom, PreparedStatement height_getter) {
		double height = 0;
		try {
			height_getter.setString(1, geom);
			height_getter.setString(2, geom);
			
			ResultSet rs = height_getter.executeQuery();
			while (rs.next())
				height = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return height;
	}
	
	public Coordinate getCoordinateFromString(String s) {
		String c = "";
		c = s.replace("POINT(", "");
		c = c.replace(")", "");
		String[] coords = c.split("\\s");
		return new Coordinate(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
	}
	
	private long getNearestWayPointID(IncidentScene scene) {
		return getNearestVertex(scene.getLon(), scene.getLat());
	}

	
	private double calculatePressureLoss() {
		int lines = Settings.getInstance().getNumberOfLines();
		double flow = Settings.getInstance().getFlowRate();
		
		double flowPerLine = flow/lines;
		double loss = 1.20536/1000000*flowPerLine*flowPerLine + 0.000855357*flowPerLine - 0.13;
		
		return loss;
	}
	
	 public float coordinateDistanceInMeters(Coordinate c1, Coordinate c2) {
		    double earthRadius = 6371000; //meters
		    double dLat = Math.toRadians(c2.getLat()-c1.getLat());
		    double dLng = Math.toRadians(c2.getLon()-c1.getLon());
		    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		               Math.cos(Math.toRadians(c1.getLat())) * Math.cos(Math.toRadians(c2.getLat())) *
		               Math.sin(dLng/2) * Math.sin(dLng/2);
		    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		    float dist = (float) (earthRadius * c);

		    return dist;
	}

	private void mouseWasClicked(MouseEvent e) {
		if (e.getClickCount() == 1
				&& e.getButton() == MouseEvent.BUTTON1) {
			
			//Punkte
			Point p = e.getPoint();
			int X = p.x;
			int Y = p.y;
			List<MapMarker> ar = map().getMapMarkerList();
			Iterator<MapMarker> i = ar.iterator();
			while (i.hasNext()) {

				MapMarker mapMarker = i.next();
				if (mapMarker.isVisible()) {
					Point markerPosition = map().getMapPosition(mapMarker.getLat(), mapMarker.getLon());
					if (markerPosition != null) {

						int centerX = markerPosition.x;
						int centerY = markerPosition.y;

						double radCircle = Math.sqrt((((centerX - X) * (centerX - X)) + (centerY - Y) * (centerY - Y)));
						if (mapMarker.getClass() == Hydrant.class) {
							if (radCircle < 7) {
								if (blockedHydrants == null) {
									blockedHydrants = new ArrayList<HosePoint>();
								}
								if (blockedHydrants.contains(selectedHoseLine.getHoses().get(0).getStart())) {
									blockedHydrants.remove(selectedHoseLine.getHoses().get(0).getStart());
								} else {
									blockedHydrants.add(selectedHoseLine.getHoses().get(0).getStart());
								}
								calculate();
								return;
							}

						}
					}
				}
			}
			
			//Wege
			// wenn kein weg angezeigt wird, abbrechen
			if (selectedWay == null) {
				return;
			}
			Point mousePoint = e.getPoint();
			
			List<WaySegment> segments = selectedWay.getWaySegments();
			if (blockedWaySegments != null) {
				for (WaySegment blo : blockedWaySegments) {
					segments.add(blo);
				}
			}
			
			double minDistance = Double.MAX_VALUE;
			
			int chosenSegment = -1;
			
			for (int j = 0; j < segments.size();j++) {
				
				Point start = map().getMapPosition(segments.get(j).getStart().getCoordinates());
				Point end = map().getMapPosition(segments.get(j).getEnd().getCoordinates());
				
				double distancePixel = Double.MAX_VALUE;
				
				try {
					distancePixel = LineToPointDistance2D(start, end, mousePoint, true);
				} catch (NullPointerException ex) {
					// nicht sichtbar
				}
				
				if (distancePixel < minDistance) {
					minDistance = distancePixel;
					chosenSegment = j;
				}
			}
			if (chosenSegment != -1 && minDistance < 4) {
				//(de-)select waypart
				if (blockedWaySegments == null) {
					blockedWaySegments = new ArrayList<WaySegment>();
				}
				
				for (WaySegment w : blockedWaySegments) {
					
					// falls es geblocked ist
					if ((w.getStart() == segments.get(chosenSegment).getStart() && w.getEnd() == segments.get(chosenSegment).getEnd()) ||
						(w.getStart() == segments.get(chosenSegment).getEnd() && w.getEnd() == segments.get(chosenSegment).getStart())) {
						blockedWaySegments.remove(w);
						// neuee strecke
						calculate();
						return;
					}
				}
				// noch nicht geblockt
				blockedWaySegments.add(segments.get(chosenSegment));
				//neue strecke
				calculate();
			}
		}
	} 
	
	public double pointToLineDistance(Point A, Point B, Point P) {
	    double normalLength = Math.sqrt((B.x-A.x)*(B.x-A.x)+(B.y-A.y)*(B.y-A.y));
	    return Math.abs((P.x-A.x)*(B.y-A.y)-(P.y-A.y)*(B.x-A.x))/normalLength;
	}
	 
	private JMapViewer map(){
		return map;
 	}
	
 	private static Coordinate c(double lat, double lon){
	 	return new Coordinate(lat, lon);
 	}
 	@Override
	public void processCommand(JMVCommandEvent arg0) {
		// TODO Auto-generated method stub
	
	}
 	
 	//Compute the dot product AB . AC
 	private double DotProduct(Point pointA, Point pointB, Point pointC)
 	{
 	    Point AB = new Point(pointB.x - pointA.x, pointB.y - pointA.y);
 	    Point BC = new Point(pointC.x - pointB.x, pointC.y - pointB.y);
 	    double dot = AB.x * BC.x + AB.y * BC.y;

 	    return dot;
 	}

 	//Compute the cross product AB x AC
 	private double CrossProduct(Point pointA, Point pointB, Point pointC)
 	{
 	    Point AB = new Point(pointB.x - pointA.x, pointB.y - pointA.y);
 	    Point AC = new Point(pointC.x - pointA.x, pointC.y - pointA.y);
 	    double cross = AB.x * AC.y - AB.y * AC.x;

 	    return cross;
 	}

 	//Compute the distance from A to B
 	private double Distance(Point  pointA, Point  pointB)
 	{
 	    double d1 = pointA.x - pointB.x;
 	    double d2 = pointA.y - pointB.y;

 	    return Math.sqrt(d1 * d1 + d2 * d2);
 	}

 	//Compute the distance from AB to C
 	//if isSegment is true, AB is a segment, not a line.
 	private double LineToPointDistance2D(Point pointA, Point pointB, Point pointC, 
 	    boolean isSegment)
 	{
 	    double dist = CrossProduct(pointA, pointB, pointC) / Distance(pointA, pointB);
 	    if (isSegment)
 	    {
 	        double dot1 = DotProduct(pointA, pointB, pointC);
 	        if (dot1 > 0) 
 	            return Distance(pointB, pointC);

 	        double dot2 = DotProduct(pointB, pointA, pointC);
 	        if (dot2 > 0) 
 	            return Distance(pointA, pointC);
 	    }
 	    return Math.abs(dist);
 	} 
}
