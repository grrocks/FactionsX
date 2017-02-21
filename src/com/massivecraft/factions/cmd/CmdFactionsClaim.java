package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Perm;
import com.massivecraft.massivecore.command.requirement.RequirementHasPerm;

public class CmdFactionsClaim extends FactionsCommand {
	// -------------------------------------------- //
	// FIELDS
	// -------------------------------------------- //

	public CmdFactionsSetOne cmdFactionsClaimOne = new CmdFactionsSetOne(true);
	public CmdFactionsSetLine cmdFactionsClaimLine = new CmdFactionsSetLine(
			true);
	public CmdFactionsSetAuto cmdFactionsClaimAuto = new CmdFactionsSetAuto(
			true);
	public CmdFactionsSetFill cmdFactionsClaimFill = new CmdFactionsSetFill(
			true);
	public CmdFactionsSetSquare cmdFactionsClaimSquare = new CmdFactionsSetSquare(
			true);
	public CmdFactionsSetCircle cmdFactionsClaimCircle = new CmdFactionsSetCircle(
			true);
	public CmdFactionsSetAll cmdFactionsClaimAll = new CmdFactionsSetAll(true);
	public CmdFactionsSetChunk cmdFactionsSetChunk = new CmdFactionsSetChunk(
			true);

	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //

	public CmdFactionsClaim() {
		// Aliases
		this.addAliases("claim");

		// Children
		this.addChild(this.cmdFactionsClaimOne);
		this.addChild(this.cmdFactionsClaimLine);
		this.addChild(this.cmdFactionsClaimAuto);
		this.addChild(this.cmdFactionsClaimFill);
		this.addChild(this.cmdFactionsClaimSquare);
		this.addChild(this.cmdFactionsClaimCircle);
		this.addChild(this.cmdFactionsClaimAll);
		this.addChild(this.cmdFactionsSetChunk);

		// Requirements
		this.addRequirements(RequirementHasPerm.get(Perm.CLAIM.node));
	}

}
