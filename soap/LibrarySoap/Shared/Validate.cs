namespace LibrarySoap.Shared;

/// <summary>Input checks shared by all features. Each one throws a ValidationFault when it fails.</summary>
public static class Validate
{
    /// <summary>Required, non-blank text up to <paramref name="maxLength"/> characters. Returns the trimmed value.</summary>
    public static string Text(string? value, string field, int maxLength)
    {
        var trimmed = value?.Trim();
        if (string.IsNullOrEmpty(trimmed)) throw Fault.Validation(field, $"'{field}' is required.");
        if (trimmed.Length > maxLength) throw Fault.Validation(field, $"'{field}' must be at most {maxLength} characters.");
        return trimmed;
    }

    /// <summary>Ids must be positive. A missing element in the XML arrives as 0 and fails here.</summary>
    public static void Id(int id, string field = "id")
    {
        if (id <= 0) throw Fault.Validation(field, $"'{field}' must be a positive integer.");
    }
}
