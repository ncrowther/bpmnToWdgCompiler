package converter.wdg.ibm.com;

public enum TaskType {
	START("start"), TASK("task"), GATEWAY("gateway"), END("end");

	private String type;

	TaskType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
