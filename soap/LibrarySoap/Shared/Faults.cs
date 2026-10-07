using System.Runtime.Serialization;
using CoreWCF;

namespace LibrarySoap.Shared;

// Fault "detail" types. Each one becomes an XSD complexType in the WSDL and is sent
// inside <s:Fault><detail>...</detail></s:Fault> when an operation fails.

[DataContract(Namespace = Soap.Namespace)]
public class NotFoundFault
{
    [DataMember(Order = 1)] public string Message { get; set; } = "";
    [DataMember(Order = 2)] public string EntityType { get; set; } = "";
    [DataMember(Order = 3)] public int EntityId { get; set; }
}

[DataContract(Namespace = Soap.Namespace)]
public class ValidationFault
{
    [DataMember(Order = 1)] public string Message { get; set; } = "";
    [DataMember(Order = 2)] public string Field { get; set; } = "";
}

[DataContract(Namespace = Soap.Namespace)]
public class ConflictFault
{
    [DataMember(Order = 1)] public string Message { get; set; } = "";
}

/// <summary>Shortcuts for throwing typed SOAP faults: <c>throw Fault.NotFound("Book", id);</c></summary>
public static class Fault
{
    public static FaultException<NotFoundFault> NotFound(string entityType, int id) =>
        new(new NotFoundFault { Message = $"{entityType} with id {id} was not found.", EntityType = entityType, EntityId = id },
            new FaultReason($"{entityType} {id} not found"));

    public static FaultException<ValidationFault> Validation(string field, string message) =>
        new(new ValidationFault { Message = message, Field = field }, new FaultReason(message));

    public static FaultException<ConflictFault> Conflict(string message) =>
        new(new ConflictFault { Message = message }, new FaultReason(message));
}
