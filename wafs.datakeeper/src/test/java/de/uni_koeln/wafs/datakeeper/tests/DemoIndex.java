package de.uni_koeln.wafs.datakeeper.tests;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.Text;



import de.uni_koeln.spinfo.wafs.mp3.data.Track;

public class DemoIndex {
	
	private Directory dir;
	private StandardAnalyzer analyzer;
	private IndexWriter writer;
	private IndexSearcher searcher;
	private DirectoryReader reader;
	
	public static final String ARTIST = "artist", ALBUM = "album", TITLE = "title", YEAR = "year", LOCATION = "location";
	
	private static final Version CURRENT = Version.LUCENE_42;
	
	
	public DemoIndex() throws IOException{
		dir = new RAMDirectory();
		analyzer = new StandardAnalyzer(CURRENT);
		IndexWriterConfig config = new IndexWriterConfig(CURRENT, analyzer);
		writer = new IndexWriter(dir, config);
	}
	
	
	public void add(Track track) throws IOException{
		Document doc = new Document();
		TextField artist = new TextField(ARTIST, track.getArtist(), Store.YES);
		doc.add(artist);
		TextField album = new TextField(ALBUM, track.getAlbum(), Store.YES);
		doc.add(album);
		TextField title = new TextField(TITLE, track.getTitle(), Store.YES);
		doc.add(title);
		IntField year = new IntField(YEAR, track.getYear(), Store.YES);
		doc.add(year);
		StringField location = new StringField(LOCATION, track.getLocation().toString(), Store.YES);
		doc.add(location);
		writer.addDocument(doc);
	}
	
	public void commit() throws IOException{
		writer.commit();
	}


	public int getSize() {
		return writer.maxDoc();
	}


	public List<Track> query(Query query) throws IOException, URISyntaxException {
		List<Track> tracks = new ArrayList<Track>();
		IndexSearcher searcher = getSearcher();
		TopDocs docs = searcher.search(query, 20);
		System.out.println("Query result: " + docs.totalHits);
		ScoreDoc[] scoreDocs = docs.scoreDocs;
		for (ScoreDoc sd : scoreDocs) {
			Document document = searcher.doc(sd.doc);
			tracks.add(toTrack(document));
		}
		return tracks;
	}


	private Track toTrack(Document document) throws URISyntaxException {
		Track track = new Track();
		track.setArtist(document.get(ARTIST));
		track.setAlbum(document.get(ALBUM));
		track.setTitle(document.get(TITLE));
		track.setYear(Integer.parseInt(document.get(YEAR)));
		track.setLocation(new URI(document.get(LOCATION)));
		return track;
	}


	private IndexSearcher getSearcher() throws IOException {
		if(searcher != null){
			return searcher;
		}
		else{
			reader = DirectoryReader.open(dir);
			searcher = new IndexSearcher(reader);
			return searcher;
		}
	}
}
