public class Campaign {
	public float cpm;
	public int id;
	public String start_date;
	public String name;
	public int clicks;
	public int views;
	
	public Campaign(float cpm, int id, String name, String start_date) {
		this.cpm = cpm;
		this.id = id;
		this.name = name;
		this.start_date = start_date;
	}
	
	public String getRevenueStr() {
		float revenue = (cpm * views) / 1000;
		return String.format("%.2f", revenue);
	}
}
