package esa.egscc.metrics.adapter.util;

import java.util.Map;

public class Tag {
	private String key;
	private String value;

	public Tag() {
	}

	public Tag(final String kvString) {
		if (kvString == null || kvString.isEmpty() || !kvString.contains("=")) {
			throw new IllegalArgumentException("Not a k=v pair: " + kvString);
		}
		final String[] kv = kvString.split("=");
		if (kv.length != 2) {
			throw new IllegalArgumentException("Not a k=v pair: " + kvString);
		}
		key = kv[0].trim();
		value = kv[1].trim();
	}

	public Tag(final String key, final String value) {
		this.key = key.trim();
		this.value = value.trim();
	}

	public Tag(final Map<String, String> tag) {
		this(tag.get("key"), tag.get("value"));
	}

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Tag tag = (Tag) o;

		if (!key.equals(tag.key)) {
			return false;
		}
		return value.equals(tag.value);
	}

	@Override
	public int hashCode() {
		int result = key.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Tag{");
		sb.append("key='").append(key).append('\'');
		sb.append(", value='").append(value).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
