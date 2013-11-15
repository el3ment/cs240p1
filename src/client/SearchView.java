package client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;

import shared.Request;

import com.sun.xml.internal.ws.util.StringUtils;

public class SearchView extends View{
	
	private SearchController _controller = new SearchController();
	private JFrame _frame;
	private QueryBuilderPanel _queryBuilderPanel;
	private FieldsList _fieldsList;
	private ResultsList _resultsList;
	private JLabel _statusText = new JLabel();
	
	public SearchView(Object listener){
		_controller.addListener(listener);
		init();
	}
	
	public SearchView(){
		init();
	}
	
	public void init(){
		_controller.setView(this);
		_controller.addListener(_controller);
	}
	
	public void create(){
		_frame = new JFrame();
		_frame.setTitle("Search");
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.setLayout(new BorderLayout());	
		
		setStatusText("Welcome " + StringUtils.capitalize((String) AppState.get().get("username")) + "!");
		
		_queryBuilderPanel = new QueryBuilderPanel();
		_fieldsList = new FieldsList();
		_resultsList = new ResultsList();
		
		_resultsList.setMinimumSize(_resultsList.getPreferredScrollableViewportSize());
		
		// Set up the event listener
		_controller.addListener(_queryBuilderPanel);
		_controller.addListener(_fieldsList);
		_controller.addListener(_resultsList);
		
		// Ask for all projects to populate the combo box
		_controller.getAllProjects();
		
		JSplitPane contentPanes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
			new JSplitPane(JSplitPane.VERTICAL_SPLIT, _queryBuilderPanel, _fieldsList),
			_resultsList);
		
		_queryBuilderPanel.setMinimumSize(_queryBuilderPanel.getPreferredSize());
		contentPanes.setMinimumSize(contentPanes.getPreferredSize());
		
		_frame.add(contentPanes, BorderLayout.CENTER);
		_frame.add(_statusText, BorderLayout.SOUTH);
		
		_frame.setMinimumSize(_frame.getPreferredSize());
		_frame.pack();
		_frame.setLocationRelativeTo(null);
		_frame.setVisible(true);
	}
	
	public void setStatusText(String text){
		_statusText.setText(text);
	}

	@SuppressWarnings("serial")
	public class QueryBuilderPanel extends JPanel{
		
		private JComboBox _projectComboBox;
		private JTextField _searchQuery;
		private JButton _searchButton;
		
		public QueryBuilderPanel(){
			super();
			
			add(new JLabel("Project Id : "));
			_projectComboBox = new JComboBox();
			_projectComboBox.setMinimumSize(_projectComboBox.getPreferredSize());
			add(_projectComboBox);
			
			add(new JLabel("Search Query : "));
			_searchQuery = new JTextField(10);
			_searchQuery.setMinimumSize(_searchQuery.getPreferredSize());
			add(_searchQuery);
			
			_searchButton = new JButton("Search");
			add(_searchButton);
			
			// When project is selected
			_projectComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0){
					Integer projectId = ((Request.GetProjectsResponse.ProjectResponse) _projectComboBox.getSelectedItem()).projectId;
					_controller.loadFields(projectId);
				}
			});
			
			// When search is selected, fire search
			_searchButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0){
					search();
				}
			});
			
			this.setMinimumSize(this.getPreferredSize());
			
		}
		
		public void search(){
			String fields = "";
			
			Object[] selectedFields = _fieldsList.getSelectedValues();
			
			// TODO : change to StringUtils.join
			String delim = "";
			for(int i = 0; i < selectedFields.length; i++){
				fields += delim + ((Request.GetFieldsResponse.FieldResponse) selectedFields[i]).fieldId;
				delim = ", ";
			}
			
			_controller.search(fields, _searchQuery.getText());
		}
		
		public void onGetProjectsSuccess(SearchController controller, Request.GetProjectsResponse response){
			Request.GetProjectsResponse.ProjectResponse allProjects = new Request.GetProjectsResponse.ProjectResponse();
			allProjects.projectId = -1;
			allProjects.title = "All Projects";
			response.projects.add(allProjects);
			
			// Add projects to combo box
			_projectComboBox.setModel(new DefaultComboBoxModel(response.projects.toArray()));
			_projectComboBox.setSelectedIndex(0);
		}
		
		public void onGetProjectsFailure(SearchController controller){
			setStatusText("There was an error retreving the list of projects");
		}
		
		// TODO : Consider moving loadFields() here to abstract the functionality away from the combo box
	}
	
	@SuppressWarnings("serial")
	public class FieldsList extends JList{
		
		public FieldsList(){
			super();
		}
		
		public void onGetFieldsSuccess(SearchController controller, Request.GetFieldsResponse params){
			Request.GetFieldsResponse.FieldResponse allFields = new Request.GetFieldsResponse.FieldResponse();
			allFields.fieldId = -1;
			allFields.title = "All Fields in All Projects";
			params.fields.add(allFields);
			this.setListData(params.fields.toArray());
		}
		public void onGetFieldsFailure(SearchController controller){
			setStatusText("There was an error getting the list of fields");
			this.setModel(new DefaultListModel());
		}
	}
	
	@SuppressWarnings("serial")
	public class ResultsList extends JList{
		
		public ResultsList(){
			
			super();
			
			// When row is double clicked - load image in new window
			this.addMouseListener(new MouseAdapter() {
			    public void mouseClicked(MouseEvent evt) {
			        JList list = (JList)evt.getSource();
			        if (evt.getClickCount() >= 2) {
			            Request.SearchResponse.SearchResult result = (Request.SearchResponse.SearchResult) list.getSelectedValue();
			            loadImage(result.imageUrl);
			        }
			    }
			});
		}
		
		public void onSearchSuccess(SearchController controller, Request.SearchResponse response){			
			// Populate the results list
			setStatusText(response.results.size() + " results found");
			this.setListData(response.results.toArray());
		}
		public void onSearchFailure(SearchController controller){
			setStatusText("No results found");
			this.setModel(new DefaultListModel());
		}
		
		// Load an image
		public void loadImage(String url){
			
			url = "http://" + AppState.get().get("hostname") + ":" + AppState.get().get("port") + "/" + url;
			JFrame imageFrame = new JFrame();
			
			try{
				BufferedImage myPicture = ImageIO.read(new URL(url));
				JLabel picLabel = new JLabel(new ImageIcon(myPicture));
				imageFrame.add(picLabel);
				imageFrame.setMinimumSize(imageFrame.getPreferredSize());
				imageFrame.setVisible(true);
			}catch(Exception e){
				imageFrame.setVisible(false);
				imageFrame.dispose();
				setStatusText("There was an error loading the image");
			}
		}
	}

}
