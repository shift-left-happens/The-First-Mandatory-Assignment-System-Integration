using System.Runtime.Serialization;
using LibrarySoap.Shared;

namespace LibrarySoap.Features.PublishingCompanies;

/// <summary>Becomes the XSD complexType "PublishingCompany" in the WSDL.</summary>
[DataContract(Namespace = Soap.Namespace)]
public class PublishingCompany
{
    [DataMember(Order = 1)] public int Id { get; set; }
    [DataMember(Order = 2)] public string Name { get; set; } = "";
}
