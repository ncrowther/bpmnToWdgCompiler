package converter.common;

public enum TaskType {
	START("start"), TASK("task"), GATEWAY("gateway"), SUBPROCESS("subprocess"), METABOT("metabot"), END("end");

	private String type;

	TaskType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
