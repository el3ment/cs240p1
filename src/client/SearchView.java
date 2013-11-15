package client;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.*;

import shared.Request;
import shared.Request.GetFieldsResponse.FieldResponse;
import shared.Request.*;
import static client.Constants.*;

import org.apache.*;

public class SearchView extends View{
	
	private SearchController _controller = new SearchController();
	private JFrame _frame;
	private QueryBuilderPanel _queryBuilderPanel;
	private FieldsList _fieldsList;
	private ResultsList _resultsList;
	
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
		
		setWelcomeText("Welcome!");
		
		_queryBuilderPanel = new QueryBuilderPanel();
		_fieldsList = new FieldsList();
		_resultsList = new ResultsList();
		
		// Set up the event listener
		_controller.addListener(_queryBuilderPanel);
		_controller.addListener(_fieldsList);
		_controller.addListener(_resultsList);
		
		_frame.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, _queryBuilderPanel, _fieldsList), BorderLayout.LINE_START);
		_frame.add(_resultsList, BorderLayout.LINE_END);
		
		_frame.setMinimumSize(_frame.getPreferredSize());
		_frame.pack();
		_frame.setVisible(true);
	}
	
	public void setWelcomeText(String text){
		_frame.add(new JLabel(text), BorderLayout.PAGE_START);
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
			
		}
		
		public void search(){
			String fields = "";
			Request.GetFieldsResponse.FieldResponse[] selectedFields = (FieldResponse[]) _fieldsList.getSelectedValues();
		
			// TODO : change to StringUtils.join
			for(int i = 0; i < selectedFields.length; i++){
				fields += selectedFields[i];
			}
			
			_controller.search(fields, _searchQuery.getText());
		}
		
		public void onGetProjectsSuccess(SearchController controller, Request.GetProjectsResponse response){
			// Add projects to combo box
			_projectComboBox.setModel(new DefaultComboBoxModel(response.projects.toArray()));
		}
		
		// TODO : Consider moving loadFields() here to abstract the functionality away from the combo box
	}
	
	@SuppressWarnings("serial")
	public class FieldsList extends JList{
		
		public FieldsList(){
			super();
		}
		
		public void onLoadFieldsSuccess(SearchController controller, Request.GetFieldsResponse params){
			this.setListData(params.fields.toArray());
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
			this.setListData(response.results.toArray());
		}
		
		public void loadImage(String url){
			System.out.println(url);
		}
	}

}
