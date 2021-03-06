/*****************************************************************************
Copyright (c) 2007-2021 - Maxprograms,  http://www.maxprograms.com/

Permission is hereby granted, free of charge, to any person obtaining a copy of 
this software and associated documentation files (the "Software"), to compile, 
modify and use the Software in its executable form without restrictions.

Redistribution of this Software or parts of it in any form (source code or 
executable binaries) requires prior written permission from Maxprograms.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
SOFTWARE.
*****************************************************************************/
package com.maxprograms.swordfish.models;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class Project implements Serializable, Comparable<Project> {

	private static final long serialVersionUID = -7301458245016833998L;

	public static final int NEW = 0;

	private String id;
	private String description;
	private int status;
	private String client;
	private String subject;
	private Language sourceLang;
	private Language targetLang;
	private LocalDate creationDate;
	private List<SourceFile> files;
	private String memory;
	private String glossary;
	private String xliff;

	public Project(String id, String description, int status, Language sourceLang, Language targetLang, String client,
			String subject, String memory, String glossary, LocalDate creationDate) {
		this.id = id;
		this.description = description;
		this.status = status;
		this.sourceLang = sourceLang;
		this.targetLang = targetLang;
		this.client = client;
		this.subject = subject;
		this.creationDate = creationDate;
		this.memory = memory;
		this.glossary = glossary;
	}

	public String getXliff() {
		return xliff;
	}

	public void setXliff(String xliff) {
		this.xliff = xliff;
	}

	public Project(JSONObject json) throws IOException {
		id = json.getString("id");
		description = json.getString("description");
		status = json.getInt("status");
		sourceLang = LanguageUtils.getLanguage(json.getString("sourceLang"));
		targetLang = LanguageUtils.getLanguage(json.getString("targetLang"));
		client = json.has("client") ? json.getString("client") : "";
		subject = json.has("subject") ? json.getString("subject") : "";
		creationDate = LocalDate.parse(json.getString("creationDate"));
		files = new ArrayList<>();
		JSONArray filesArray = json.getJSONArray("files");
		for (int i = 0; i < filesArray.length(); i++) {
			files.add(new SourceFile(filesArray.getJSONObject(i)));
		}
		xliff = json.getString("xliff");
		memory = json.getString("memory");
		glossary = json.getString("glossary");
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("description", description);
		json.put("status", status);
		json.put("sourceLang", sourceLang.getCode());
		json.put("targetLang", targetLang.getCode());
		json.put("client", client);
		json.put("subject", subject);
		json.put("creationDate", creationDate.toString());
		JSONArray filesArray = new JSONArray();
		Iterator<SourceFile> it = files.iterator();
		while (it.hasNext()) {
			filesArray.put(it.next().toJSON());
		}
		json.put("files", filesArray);
		json.put("xliff", xliff);
		json.put("memory", memory);
		json.put("glossary", glossary);
		return json;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Language getSourceLang() {
		return sourceLang;
	}

	public void setSourceLang(Language sourceLang) {
		this.sourceLang = sourceLang;
	}

	public Language getTargetLang() {
		return targetLang;
	}

	public void setTargetLang(Language targetLang) {
		this.targetLang = targetLang;
	}

	public LocalDate getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	public List<SourceFile> getFiles() {
		return files;
	}

	public void setFiles(List<SourceFile> files) {
		this.files = files;
	}

	public void setFiles(JSONArray array) {
		files = new ArrayList<>();
		for (int i=0 ; i<array.length() ; i++) {
			files.add(new SourceFile(array.getJSONObject(i)));
		}
	}

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getGlossary() {
		return glossary;
	}

	public void setGlossary(String glossary) {
		this.glossary = glossary;
	}

	public String getClient() {
		return client;
	}

	public String getSubject() {
		return subject;
	}

	@Override
	public int compareTo(Project o) {
		return creationDate.compareTo(o.getCreationDate());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Project)) {
			return false;
		}
		Project p = (Project) obj;
		return id.equals(p.getId());
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
