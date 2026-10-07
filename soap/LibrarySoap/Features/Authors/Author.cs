using System.Runtime.Serialization;
using LibrarySoap.Shared;

namespace LibrarySoap.Features.Authors;

/// <summary>Becomes the XSD complexType "Author" in the WSDL.</summary>
[DataContract(Namespace = Soap.Namespace)]
public class Author
{
    [DataMember(Order = 1)] public int Id { get; set; }
    [DataMember(Order = 2)] public string Name { get; set; } = "";
    [DataMember(Order = 3)] public string Surname { get; set; } = "";
}
