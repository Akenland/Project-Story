package com.kylenanakdewa.story.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * A Book made up of JSON pages.
 * @author Kyle Nanakdewa
 */
public class Book {

	/** The book title. */
	private String title;
	/** The book author. */
	private String author;

	/** The pages in the book, as JSON strings. */
	private List<String> pages;

	public Book(String title, String author){
		this.title = title;
		this.author = author;
		pages = new ArrayList<String>();
	}


	/**
	 * Gets the JSON string for this book.
	 */
	public String getJsonString(){
		String pageString = "";
		for(String page : pages) pageString += "\""+page+"\",";
		pageString = pageString.substring(0, pageString.length());

		return "{author:\""+author+"\",title:\""+title+"\",pages:["+pageString+"]}";
	}
	/**
	 * Gets the ItemStack of this book.
	 */
	@SuppressWarnings("deprecation")
	public ItemStack getItem(){
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		Bukkit.getUnsafe().modifyItemStack(book, getJsonString());
		return book;
	}


	/**
	 * Gets the title of this book.
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * Sets the title of this book.
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets the author of this book.
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}
	/**
	 * Sets the author of this book.
	 * @param author the new author
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * Gets the JSON strings representing pages of this book.
	 * @return the list of pages, as JSON strings
	 */
	public List<String> getPages() {
		return pages;
	}
	/**
	 * Sets the JSON strings representing pages of this book.
	 * @param pages the new list of pages, as JSON strings
	 */
	public void setPages(List<String> pages) {
		this.pages = pages;
	}
	/**
	 * Adds a page to this book.
	 * @param content the plain text for this page
	 */
	public void addSimplePage(String text){
		addRawPage("text:\""+text+"\"");
	}
	/**
	 * Adds a page to this book.
	 * @param rawJson the raw JSON content for this page
	 */
	public void addRawPage(String rawJson){
		pages.add(rawJson);
	}

}