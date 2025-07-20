package com.yakrooms.be.model.entity;

import jakarta.persistence.*;

@Entity
public class RoomItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name; // e.g., "Queen Bed", "TV", "Electric Kettle"

	private String description; // optional, e.g., "Smart TV with Netflix", etc.

	private String icon; // optional: used for frontend (FontAwesome class or image URL)

	@ManyToOne
	@JoinColumn(name = "room_id")
	private Room room;

	public RoomItem() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

}
