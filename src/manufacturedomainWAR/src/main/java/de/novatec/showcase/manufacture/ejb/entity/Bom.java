package de.novatec.showcase.manufacture.ejb.entity;

import de.novatec.showcase.manufacture.ejb.encryptor.BomDescriptorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Objects;


@Table(name = "M_BOM")
@Entity
@IdClass(BomPK.class)
@NamedQuery(name = Bom.ALL_BOMS, query = Bom.ALL_BOMS_QUERY)
@EntityListeners(BomDescriptorListener.class)
public class Bom {

	private static final Logger LOG = LoggerFactory.getLogger(Bom.class);

	public static final String  ALL_BOMS = "ALL_BOMS";
	
	public static final String ALL_BOMS_QUERY = "Select b From Bom b";
	
	@Id
	@Column(name = "B_COMP_ID", length = 20, nullable = false)
	private String componentId;

	@Id
	@Column(name = "B_ASSEMBLY_ID", length = 20, nullable = false)
	private String assemblyId;
	
	@Id
	@Column(name = "B_LINE_NO", nullable = false)
	private int lineNo;

	@Column(name = "DEK")
	private String dataEncryptionKeyEncrypted;

	@Transient
	private byte[] dataEncryptionKey; // decrypted value

	@Column(name = "B_QTY")
	private String quantityEncrypted;

	@Transient
	private int quantity; // decrypted value
	
	@Column(name="B_ENG_CHANGE", length = 10)
	private String engChangeEncrypted;

	@Transient
	private String engChange;
	
	@Column(name="B_OPS")
	private int opsNo;

	@Column(name="B_OPS_DESC", length = 100)
	private String opsDescEncrypted;

	@Transient
	private String opsDesc;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="B_COMP_ID",insertable=false,updatable=false)
	private Component component;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="B_ASSEMBLY_ID",insertable=false,updatable=false)
	private Assembly assembly;
	
	@Version
	@Column(name="B_VERSION")
	private Integer version;

	public Bom() {
		super();
	}

	public Bom(int lineNo, int quantity, String engChange, int opsNo,
			String opsDesc, Component component, Assembly assembly) {
		super();
		this.lineNo =lineNo;
		this.assemblyId = assembly.getId();
		this.componentId = component.getId();
		this.quantity = quantity;
		this.engChange = engChange;
		this.opsNo = opsNo;
		this.opsDesc = opsDesc;
		this.component = component;
		this.assembly = assembly;
	}
	
	public String getComponentId() {
		return componentId;
	}

	public String getAssemblyId() {
		return assemblyId;
	}
	
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public void setAssemblyId(String assemblyId) {
		this.assemblyId = assemblyId;
	}

	public int getLineNo(){
		return this.lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getEngChange() {
		return engChange;
	}

	public void setEngChange(String engChange) {
		this.engChange = engChange;
	}

	public int getOpsNo() {
		return opsNo;
	}

	public void setOpsNo(int opsNo) {
		this.opsNo = opsNo;
	}

	public String getOpsDesc() {
		return opsDesc;
	}

	public void setOpsDesc(String opsDesc) {
		this.opsDesc = opsDesc;
	}

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}
	
	public Assembly getAssembly() {
		return assembly;
	}

	public void setAssembly(Assembly assembly) {
		this.assembly = assembly;
	}

	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(Integer version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				assemblyId, 
				componentId, engChange, lineNo, opsDesc, opsNo, quantity,
				version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Bom)) {
			return false;
		}
		Bom other = (Bom) obj;
		return Objects.equals(assemblyId, other.assemblyId)
				&& Objects.equals(componentId, other.componentId)
				&& Objects.equals(engChange, other.engChange) && lineNo == other.lineNo
				&& Objects.equals(opsDesc, other.opsDesc) && opsNo == other.opsNo && quantity == other.quantity
				&& version == other.version;
	}

	@Override
	public String toString() {
		return "Bom [componentId=" + componentId + ", assemblyId=" + assemblyId + ", lineNo=" + lineNo + ", quantity="
				+ quantity + ", engChange=" + engChange + ", opsNo=" + opsNo + ", opsDesc=" + opsDesc + ", version="
				+ version + "]";
	}

	public String getDataEncryptionKeyEncrypted() {
		return dataEncryptionKeyEncrypted;
	}

	public void setDataEncryptionKeyEncrypted(String dataEncryptionKeyEncrypted) {
		this.dataEncryptionKeyEncrypted = dataEncryptionKeyEncrypted;
	}

	public byte[] getDataEncryptionKey() {
		return dataEncryptionKey;
	}

	public void setDataEncryptionKey(byte[] dataEncryptionKey) {
		this.dataEncryptionKey = dataEncryptionKey;
	}

	public String getQuantityEncrypted() {
		return quantityEncrypted;
	}

	public void setQuantityEncrypted(String quantityEncrypted) {
		this.quantityEncrypted = quantityEncrypted;
	}

	public String getEngChangeEncrypted() {
		return engChangeEncrypted;
	}

	public void setEngChangeEncrypted(String engChangeEncrypted) {
		this.engChangeEncrypted = engChangeEncrypted;
	}

	public String getOpsDescEncrypted() {
		return opsDescEncrypted;
	}

	public void setOpsDescEncrypted(String opsDescEncrypted) {
		this.opsDescEncrypted = opsDescEncrypted;
	}
}
